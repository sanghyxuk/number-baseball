package com.couple.numberbaseball.listener;

import com.couple.numberbaseball.dto.websocket.*;
import com.couple.numberbaseball.model.GameRoom;
import com.couple.numberbaseball.model.GameTurn;
import com.couple.numberbaseball.service.GameRoomService;
import com.couple.numberbaseball.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 연결 상태 관리 리스너
 * 플레이어 연결/연결 끊김 처리 및 재접속 관리
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private SessionService sessionService;

    // WebSocket 세션 ID -> 플레이어 세션 ID 매핑
    private final ConcurrentHashMap<String, String> webSocketToPlayerSession = new ConcurrentHashMap<>();

    // 연결이 끊어진 플레이어들의 재접속 대기 관리
    private final ConcurrentHashMap<String, DisconnectedPlayer> disconnectedPlayers = new ConcurrentHashMap<>();

    // 재접속 타임아웃 관리용 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final int RECONNECTION_TIMEOUT_MINUTES = 5;

    /**
     * WebSocket 연결 이벤트 처리
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String webSocketSessionId = headerAccessor.getSessionId();

        // 헤더에서 플레이어 세션 ID 추출
        String playerSessionId = headerAccessor.getFirstNativeHeader("X-Player-Session-Id");

        if (playerSessionId != null && sessionService.isValidSession(playerSessionId)) {
            webSocketToPlayerSession.put(webSocketSessionId, playerSessionId);

            // 재접속인지 확인
            DisconnectedPlayer disconnectedPlayer = disconnectedPlayers.remove(playerSessionId);
            if (disconnectedPlayer != null) {
                handlePlayerReconnected(playerSessionId, disconnectedPlayer.getRoomCode());
            } else {
                handlePlayerConnected(playerSessionId);
            }

            System.out.println("WebSocket 연결됨: " + webSocketSessionId + " -> 플레이어: " + playerSessionId);
        }
    }

    /**
     * WebSocket 연결 끊김 이벤트 처리
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String webSocketSessionId = headerAccessor.getSessionId();

        String playerSessionId = webSocketToPlayerSession.remove(webSocketSessionId);

        if (playerSessionId != null) {
            handlePlayerDisconnected(playerSessionId);
            System.out.println("WebSocket 연결 끊김: " + webSocketSessionId + " -> 플레이어: " + playerSessionId);
        }
    }

    /**
     * 플레이어 연결 처리
     */
    private void handlePlayerConnected(String playerSessionId) {
        GameRoom room = gameRoomService.findRoomBySessionId(playerSessionId);
        if (room != null) {
            // 연결 상태 브로드캐스트
            PlayerConnectionPayloadDto connectionPayload = new PlayerConnectionPayloadDto(
                    playerSessionId,
                    sessionService.getPlayerNickname(playerSessionId),
                    true,
                    "CONNECTED"
            );

            broadcastToRoom(room.getRoomId(), WebSocketMessageType.PLAYER_CONNECTED, connectionPayload);
        }
    }

    /**
     * 플레이어 연결 끊김 처리
     */
    private void handlePlayerDisconnected(String playerSessionId) {
        GameRoom room = gameRoomService.findRoomBySessionId(playerSessionId);
        if (room == null) {
            return; // 방에 속해있지 않은 경우
        }

        // 게임이 활성 상태인 경우에만 재접속 대기 처리
        if (room.getStatus().isActive()) {
            // 재접속 대기 상태로 설정
            DisconnectedPlayer disconnectedPlayer = new DisconnectedPlayer(
                    playerSessionId,
                    room.getRoomId(),
                    System.currentTimeMillis()
            );
            disconnectedPlayers.put(playerSessionId, disconnectedPlayer);

            // 재접속 타임아웃 스케줄링
            scheduler.schedule(() -> {
                handleReconnectionTimeout(playerSessionId);
            }, RECONNECTION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            // 연결 끊김 상태 브로드캐스트
            PlayerConnectionPayloadDto connectionPayload = new PlayerConnectionPayloadDto(
                    playerSessionId,
                    sessionService.getPlayerNickname(playerSessionId),
                    false,
                    "DISCONNECTED"
            );

            broadcastToRoom(room.getRoomId(), WebSocketMessageType.PLAYER_DISCONNECTED, connectionPayload);

        } else {
            // 게임이 비활성 상태면 그냥 방에서 제거
            gameRoomService.leaveRoom(playerSessionId);
        }
    }

    /**
     * 플레이어 재접속 처리
     */
    private void handlePlayerReconnected(String playerSessionId, String roomCode) {
        GameRoom room = gameRoomService.findRoomByCode(roomCode);
        if (room != null && room.isPlayerInRoom(playerSessionId)) {
            // 재접속 성공 브로드캐스트
            PlayerConnectionPayloadDto connectionPayload = new PlayerConnectionPayloadDto(
                    playerSessionId,
                    sessionService.getPlayerNickname(playerSessionId),
                    true,
                    "RECONNECTED"
            );

            broadcastToRoom(roomCode, WebSocketMessageType.PLAYER_CONNECTED, connectionPayload);

            // 현재 게임 상태를 재접속한 플레이어에게 전송
            sendGameStateToPlayer(playerSessionId, room);
        }
    }

    /**
     * 재접속 타임아웃 처리
     */
    private void handleReconnectionTimeout(String playerSessionId) {
        DisconnectedPlayer disconnectedPlayer = disconnectedPlayers.remove(playerSessionId);
        if (disconnectedPlayer == null) {
            return; // 이미 재접속했거나 처리됨
        }

        GameRoom room = gameRoomService.findRoomByCode(disconnectedPlayer.getRoomCode());
        if (room != null) {
            // 상대방 찾기
            String opponentSessionId = playerSessionId.equals(room.getCreatorSessionId()) ?
                    room.getJoinerSessionId() : room.getCreatorSessionId();

            // 게임 종료 처리 (타임아웃으로 인한 포기)
            room.abandonGame(playerSessionId);
            gameRoomService.updateRoom(room);

            // 게임 종료 브로드캐스트
            GameFinishedPayloadDto finishPayload = new GameFinishedPayloadDto(
                    opponentSessionId, // 상대방이 승리
                    "TIMEOUT",
                    room.getHistory()
            );

            broadcastToRoom(room.getRoomId(), WebSocketMessageType.GAME_FINISHED, finishPayload);
        }

        // 세션 정리
        sessionService.removeSession(playerSessionId);
    }

    /**
     * 현재 게임 상태를 특정 플레이어에게 전송
     */
    private void sendGameStateToPlayer(String playerSessionId, GameRoom room) {
        // 게임 상태 정보 생성
        StateChangePayloadDto statePayload = new StateChangePayloadDto(
                room.getStatus(),
                room.getCurrentTurn(),
                room.getRoomId(),
                room.isCreatorReady(),
                room.isJoinerReady()
        );

        WebSocketMessageDto<StateChangePayloadDto> message =
                new WebSocketMessageDto<>(WebSocketMessageType.STATE_CHANGE, statePayload);

        messagingTemplate.convertAndSendToUser(playerSessionId, "/queue/game-state", message);

        // 게임 기록도 전송 (게임 진행 중인 경우)
        if (room.getStatus().isActive() && !room.getHistory().isEmpty()) {
            for (GameTurn turn : room.getHistory()) {
                NewGuessPayloadDto guessPayload = NewGuessPayloadDto.fromGameTurn(turn, room.getCurrentTurn());
                WebSocketMessageDto<NewGuessPayloadDto> turnMessage =
                        new WebSocketMessageDto<>(WebSocketMessageType.NEW_GUESS, guessPayload);

                messagingTemplate.convertAndSendToUser(playerSessionId, "/queue/game-history", turnMessage);
            }
        }
    }

    /**
     * 방의 모든 플레이어에게 메시지 브로드캐스트
     */
    private <T> void broadcastToRoom(String roomCode, WebSocketMessageType messageType, T payload) {
        WebSocketMessageDto<T> message = new WebSocketMessageDto<>(messageType, payload);
        messagingTemplate.convertAndSend("/topic/game/" + roomCode + "/sync", message);
    }

    /**
     * 연결이 끊어진 플레이어 정보 클래스
     */
    private static class DisconnectedPlayer {
        private final String playerSessionId;
        private final String roomCode;
        private final long disconnectedAt;

        public DisconnectedPlayer(String playerSessionId, String roomCode, long disconnectedAt) {
            this.playerSessionId = playerSessionId;
            this.roomCode = roomCode;
            this.disconnectedAt = disconnectedAt;
        }

        public String getPlayerSessionId() { return playerSessionId; }
        public String getRoomCode() { return roomCode; }
        public long getDisconnectedAt() { return disconnectedAt; }
    }

    /**
     * 리소스 정리 (애플리케이션 종료시)
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}