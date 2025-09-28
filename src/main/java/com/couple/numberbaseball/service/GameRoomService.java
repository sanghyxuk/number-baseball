package com.couple.numberbaseball.service;

import com.couple.numberbaseball.model.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 게임 방 관리 서비스
 * In-Memory Storage로 게임 방들을 관리
 */
@Service
public class GameRoomService {

    // In-Memory Storage: 방 코드 -> GameRoom
    private final Map<String, GameRoom> gameRooms = new ConcurrentHashMap<>();

    // 세션 ID -> 방 코드 매핑 (빠른 조회용)
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    private final Random random = new Random();

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용

    private static final int ROOM_CODE_LENGTH = 6;
    private static final int ROOM_TIMEOUT_MINUTES = 5; // 5분 비활성화시 방 삭제

    /**
     * 새 게임 방 생성
     * @param creatorSessionId 방장의 세션 ID
     * @param settings 게임 설정
     * @return 생성된 게임 방
     */
    public GameRoom createRoom(String creatorSessionId, GameSettings settings) {
        // 기존에 참여중인 방이 있다면 삭제
        leaveRoom(creatorSessionId);

        String roomCode = generateUniqueRoomCode();
        GameRoom room = new GameRoom(roomCode, creatorSessionId, settings);

        gameRooms.put(roomCode, room);
        sessionToRoom.put(creatorSessionId, roomCode);

        return room;
    }

    /**
     * 방에 참가
     * @param roomCode 방 코드
     * @param joinerSessionId 참가자 세션 ID
     * @return 참가 성공한 게임 방, 실패시 null
     */
    public GameRoom joinRoom(String roomCode, String joinerSessionId) {
        GameRoom room = gameRooms.get(roomCode);

        if (room == null) {
            return null; // 방이 존재하지 않음
        }

        if (!room.getStatus().canJoin()) {
            return null; // 참가할 수 없는 상태
        }

        // 기존에 참여중인 방이 있다면 삭제
        leaveRoom(joinerSessionId);

        if (room.joinRoom(joinerSessionId)) {
            sessionToRoom.put(joinerSessionId, roomCode);

            // --- [수정된 부분] ---
            // 참가 성공 후 모든 클라이언트에게 상태 변경 알림
            broadcastStateChange(roomCode);
            // --- [수정 끝] ---

            return room;
        }

        return null; // 참가 실패
    }

    /**
     * 특정 방의 상태 변경을 모든 클라이언트에게 브로드캐스트
     * @param roomCode 방 코드
     */
    public void broadcastStateChange(String roomCode) {
        GameRoom room = findRoomByCode(roomCode);
        if (room != null) {
            StateChangePayload payload = new StateChangePayload(
                    room.getStatus(),
                    room.getCurrentTurn(),
                    room.getRoomId(),
                    room.isCreatorReady(),
                    room.isJoinerReady()
            );

            WebSocketMessage<StateChangePayload> message = new WebSocketMessage<>(
                    WebSocketMessageType.STATE_CHANGE,
                    payload
            );

            String destination = "/topic/game/" + roomCode + "/sync";
            System.out.println("Broadcasting to " + destination + ": " + message);
            messagingTemplate.convertAndSend(destination, message);
        }
    }


    /**
     * 방 떠나기
     * @param sessionId 세션 ID
     */
    public void leaveRoom(String sessionId) {
        String roomCode = sessionToRoom.get(sessionId);
        if (roomCode != null) {
            GameRoom room = gameRooms.get(roomCode);
            if (room != null) {
                String opponentSessionId = null;
                // 방에서 플레이어 제거 로직
                if (room.getCreatorSessionId().equals(sessionId)) {
                    // 방장이 나가면 방 삭제
                    opponentSessionId = room.getJoinerSessionId();
                    removeRoom(roomCode);
                } else if (sessionId.equals(room.getJoinerSessionId())) {
                    // 참가자가 나가면 다시 대기 상태로
                    opponentSessionId = room.getCreatorSessionId();
                    room.setJoinerSessionId(null);
                    room.setStatus(GameStatus.WAITING_FOR_JOINER);
                    sessionToRoom.remove(sessionId);
                    // 상태 변경 알림
                    broadcastStateChange(roomCode);
                }
            }
        }
    }

    /**
     * 세션 ID로 현재 참여중인 방 찾기
     * @param sessionId 세션 ID
     * @return 참여중인 게임 방, 없으면 null
     */
    public GameRoom findRoomBySessionId(String sessionId) {
        String roomCode = sessionToRoom.get(sessionId);
        if (roomCode != null) {
            return gameRooms.get(roomCode);
        }
        return null;
    }

    /**
     * 방 코드로 방 찾기
     * @param roomCode 방 코드
     * @return 게임 방, 없으면 null
     */
    public GameRoom findRoomByCode(String roomCode) {
        return gameRooms.get(roomCode);
    }

    /**
     * 6자리 유니크한 방 코드 생성
     * @return 생성된 방 코드
     */
    private String generateUniqueRoomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code;

        do {
            code = new StringBuilder();
            for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (gameRooms.containsKey(code.toString()));

        return code.toString();
    }

    /**
     * 방 삭제
     * @param roomCode 방 코드
     */
    private void removeRoom(String roomCode) {
        GameRoom room = gameRooms.remove(roomCode);
        if (room != null) {
            // 관련 세션 매핑도 삭제
            if (room.getCreatorSessionId() != null) {
                sessionToRoom.remove(room.getCreatorSessionId());
            }
            if (room.getJoinerSessionId() != null) {
                sessionToRoom.remove(room.getJoinerSessionId());
            }
        }
    }


    /**
     * 비활성화된 방들 정리 (스케줄러에서 호출)
     */
    public void cleanupInactiveRooms() {
        LocalDateTime timeout = LocalDateTime.now().minus(ROOM_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        List<String> roomsToRemove = new ArrayList<>();

        for (Map.Entry<String, GameRoom> entry : gameRooms.entrySet()) {
            GameRoom room = entry.getValue();
            if (room.getLastActivity().isBefore(timeout)) {
                roomsToRemove.add(entry.getKey());
            }
        }

        for (String roomCode : roomsToRemove) {
            removeRoom(roomCode);
        }

        if (!roomsToRemove.isEmpty()) {
            System.out.println("Cleaned up " + roomsToRemove.size() + " inactive rooms");
        }
    }

    /**
     * 현재 활성 방 개수 조회
     * @return 활성 방 개수
     */
    public int getActiveRoomCount() {
        return gameRooms.size();
    }

    /**
     * 디버깅용: 모든 방 정보 조회
     * @return 모든 방 정보
     */
    public Map<String, GameRoom> getAllRooms() {
        return new HashMap<>(gameRooms);
    }

    /**
     * 특정 세션이 특정 방에 속해있는지 확인
     * @param sessionId 세션 ID
     * @param roomCode 방 코드
     * @return 속해있으면 true
     */
    public boolean isSessionInRoom(String sessionId, String roomCode) {
        GameRoom room = gameRooms.get(roomCode);
        return room != null && room.isPlayerInRoom(sessionId);
    }

    /**
     * 게임 방 상태 업데이트 (외부에서 직접 수정 후 호출)
     * @param room 업데이트된 게임 방
     */
    public void updateRoom(GameRoom room) {
        if (room != null && room.getRoomId() != null) {
            gameRooms.put(room.getRoomId(), room);
        }
    }
}
