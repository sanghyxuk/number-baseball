package com.couple.numberbaseball.controller;

import com.couple.numberbaseball.dto.websocket.*;
import com.couple.numberbaseball.model.GameRoom;
import com.couple.numberbaseball.model.GameStatus;
import com.couple.numberbaseball.model.GameTurn;
import com.couple.numberbaseball.service.GameLogicService;
import com.couple.numberbaseball.service.GameRoomService;
import com.couple.numberbaseball.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
/**
 * WebSocket 메시지 처리 컨트롤러
 * 실시간 게임 상태 동기화 및 게임 플레이 로직 처리
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private GameLogicService gameLogicService;

    /**
     * 플레이어 준비 상태 설정
     * /app/game/ready
     */
    @MessageMapping("/game/ready")
    public void handlePlayerReady(@Payload Map<String, Object> message) {
        try {
            String sessionId = (String) message.get("sessionId");
            Boolean ready = (Boolean) message.get("ready");

            // 입력 검증
            if (sessionId == null || ready == null) {
                sendErrorToUser(sessionId, "INVALID_REQUEST", "세션 ID 또는 준비 상태가 누락되었습니다.");
                return;
            }

            // 세션 검증
            if (!sessionService.isValidSession(sessionId)) {
                sendErrorToUser(sessionId, "INVALID_SESSION", "유효하지 않은 세션입니다.");
                return;
            }

            // 현재 참가중인 방 찾기
            GameRoom room = gameRoomService.findRoomBySessionId(sessionId);
            if (room == null) {
                sendErrorToUser(sessionId, "NO_ROOM", "참가중인 방이 없습니다.");
                return;
            }

            // 준비 상태 가능한지 확인
            if (room.getStatus() != GameStatus.WAITING_FOR_READY) {
                sendErrorToUser(sessionId, "INVALID_STATE", "준비 상태를 설정할 수 없는 게임 상태입니다.");
                return;
            }

            // 준비 상태 설정
            room.setPlayerReady(sessionId, ready);
            gameRoomService.updateRoom(room);

            // 준비 상태 변경 브로드캐스트
            PlayerReadyPayloadDto readyPayload = new PlayerReadyPayloadDto(
                    sessionId,
                    ready,
                    sessionService.getPlayerNickname(sessionId)
            );

            broadcastToRoom(room.getRoomId(), WebSocketMessageType.PLAYER_READY, readyPayload);

            // 게임 상태 변경 브로드캐스트 (둘 다 준비되었을 경우)
            if (room.getStatus() == GameStatus.SETTING_ANSWERS) {
                broadcastGameStateChange(room);
            }

        } catch (Exception e) {
            String sessionId = (String) message.get("sessionId");
            sendErrorToUser(sessionId, "SERVER_ERROR", "준비 상태 설정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 플레이어 정답 설정
     * /app/game/{roomCode}/setAnswer
     */
    @MessageMapping("/game/{roomCode}/setAnswer")
    public void handleSetAnswer(@DestinationVariable String roomCode,
                                @Payload Map<String, Object> message) {
        try {
            String sessionId = (String) message.get("sessionId");
            String answer = (String) message.get("answer");

            // 입력 검증
            if (sessionId == null || answer == null) {
                sendErrorToUser(sessionId, "INVALID_REQUEST", "세션 ID 또는 정답이 누락되었습니다.");
                return;
            }

            // 세션 및 방 검증
            if (!validateSessionAndRoom(sessionId, roomCode)) {
                return;
            }

            GameRoom room = gameRoomService.findRoomByCode(roomCode);

            // 게임 상태 확인
            if (room.getStatus() != GameStatus.SETTING_ANSWERS) {
                sendErrorToUser(sessionId, "INVALID_STATE", "정답을 설정할 수 없는 게임 상태입니다.");
                return;
            }

            // 정답 유효성 검증
            if (!gameLogicService.isValidAnswer(answer, room.getSettings())) {
                sendErrorToUser(sessionId, "INVALID_ANSWER", "유효하지 않은 정답입니다. 게임 설정을 확인해주세요.");
                return;
            }

            // 정답 설정
            room.setPlayerAnswer(sessionId, answer);
            gameRoomService.updateRoom(room);

            // 정답 설정 완료 알림
            boolean allAnswersSet = (room.getCreatorAnswer() != null && room.getJoinerAnswer() != null);
            AnswerSetPayloadDto answerPayload = new AnswerSetPayloadDto(sessionId, true, allAnswersSet);

            broadcastToRoom(roomCode, WebSocketMessageType.ANSWER_SET, answerPayload);

            // 모든 플레이어가 정답을 설정했으면 게임 시작
            if (room.getStatus() == GameStatus.IN_PROGRESS) {
                broadcastGameStateChange(room);
            }

        } catch (Exception e) {
            String sessionId = (String) message.get("sessionId");
            sendErrorToUser(sessionId, "SERVER_ERROR", "정답 설정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 게임 추측 처리
     * /app/game/{roomCode}/guess
     */
    @MessageMapping("/game/{roomCode}/guess")
    public void handleGuess(@DestinationVariable String roomCode,
                            @Payload Map<String, Object> message) {
        try {
            String sessionId = (String) message.get("sessionId");
            String guess = (String) message.get("guess");

            // 입력 검증
            if (sessionId == null || guess == null) {
                sendErrorToUser(sessionId, "INVALID_REQUEST", "세션 ID 또는 추측이 누락되었습니다.");
                return;
            }

            // 세션 및 방 검증
            if (!validateSessionAndRoom(sessionId, roomCode)) {
                return;
            }

            GameRoom room = gameRoomService.findRoomByCode(roomCode);

            // 게임 상태 확인
            if (room.getStatus() != GameStatus.IN_PROGRESS) {
                sendErrorToUser(sessionId, "INVALID_STATE", "게임이 진행 중이 아닙니다.");
                return;
            }

            // 현재 턴인지 확인
            if (!room.isPlayerTurn(sessionId)) {
                sendErrorToUser(sessionId, "NOT_YOUR_TURN", "현재 당신의 턴이 아닙니다.");
                return;
            }

            // 추측 유효성 검증
            if (!gameLogicService.isValidInput(guess, room.getSettings())) {
                sendErrorToUser(sessionId, "INVALID_GUESS", "유효하지 않은 추측입니다. 게임 설정을 확인해주세요.");
                return;
            }

            // 상대방의 정답 가져오기
            String targetAnswer;
            if (sessionId.equals(room.getCreatorSessionId())) {
                targetAnswer = room.getJoinerAnswer();
            } else {
                targetAnswer = room.getCreatorAnswer();
            }

            // 판정 수행
            String result = gameLogicService.judge(guess, targetAnswer);

            // 게임 턴 추가
            room.addTurn(sessionId, guess, result);
            gameRoomService.updateRoom(room);

            // 최신 턴 정보 가져오기
            GameTurn latestTurn = room.getHistory().get(room.getHistory().size() - 1);

            // 새로운 추측 결과 브로드캐스트
            NewGuessPayloadDto guessPayload = NewGuessPayloadDto.fromGameTurn(latestTurn, room.getCurrentTurn());
            broadcastToRoom(roomCode, WebSocketMessageType.NEW_GUESS, guessPayload);

            // 게임 종료 확인
            if (room.getStatus() == GameStatus.FINISHED) {
                // 승리 메시지 브로드캐스트
                GameFinishedPayloadDto finishPayload = new GameFinishedPayloadDto(
                        sessionId, // 현재 추측한 플레이어가 승리자
                        "WIN",
                        room.getHistory()
                );
                broadcastToRoom(roomCode, WebSocketMessageType.GAME_FINISHED, finishPayload);
            }

        } catch (Exception e) {
            String sessionId = (String) message.get("sessionId");
            sendErrorToUser(sessionId, "SERVER_ERROR", "추측 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 게임 포기 처리
     * /app/game/{roomCode}/abandon
     */
    @MessageMapping("/game/{roomCode}/abandon")
    public void handleAbandon(@DestinationVariable String roomCode,
                              @Payload Map<String, Object> message) {
        try {
            String sessionId = (String) message.get("sessionId");

            // 세션 및 방 검증
            if (!validateSessionAndRoom(sessionId, roomCode)) {
                return;
            }

            GameRoom room = gameRoomService.findRoomByCode(roomCode);

            // 게임 포기 처리
            room.abandonGame(sessionId);
            gameRoomService.updateRoom(room);

            // 상대방 찾기 (승리자)
            String winner = sessionId.equals(room.getCreatorSessionId()) ?
                    room.getJoinerSessionId() : room.getCreatorSessionId();

            // 게임 종료 브로드캐스트
            GameFinishedPayloadDto finishPayload = new GameFinishedPayloadDto(
                    winner,
                    "ABANDON",
                    room.getHistory()
            );
            broadcastToRoom(roomCode, WebSocketMessageType.GAME_FINISHED, finishPayload);

        } catch (Exception e) {
            String sessionId = (String) message.get("sessionId");
            sendErrorToUser(sessionId, "SERVER_ERROR", "게임 포기 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // === 유틸리티 메서드 ===

    /**
     * 세션과 방 유효성 검증
     */
    private boolean validateSessionAndRoom(String sessionId, String roomCode) {
        // 세션 검증
        if (!sessionService.isValidSession(sessionId)) {
            sendErrorToUser(sessionId, "INVALID_SESSION", "유효하지 않은 세션입니다.");
            return false;
        }

        // 방 존재 확인
        GameRoom room = gameRoomService.findRoomByCode(roomCode);
        if (room == null) {
            sendErrorToUser(sessionId, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다.");
            return false;
        }

        // 플레이어가 방에 속해있는지 확인
        if (!room.isPlayerInRoom(sessionId)) {
            sendErrorToUser(sessionId, "NOT_IN_ROOM", "해당 방에 속해있지 않습니다.");
            return false;
        }

        return true;
    }

    /**
     * 방의 모든 플레이어에게 메시지 브로드캐스트
     */
    private <T> void broadcastToRoom(String roomCode, WebSocketMessageType messageType, T payload) {
        WebSocketMessageDto<T> message = new WebSocketMessageDto<>(messageType, payload);
        messagingTemplate.convertAndSend("/topic/game/" + roomCode + "/sync", message);
    }

    /**
     * 게임 상태 변경 브로드캐스트
     */
    private void broadcastGameStateChange(GameRoom room) {
        StateChangePayloadDto statePayload = new StateChangePayloadDto(
                room.getStatus(),
                room.getCurrentTurn(),
                room.getRoomId(),
                room.isCreatorReady(),
                room.isJoinerReady()
        );

        broadcastToRoom(room.getRoomId(), WebSocketMessageType.STATE_CHANGE, statePayload);
    }

    /**
     * 특정 사용자에게 오류 메시지 전송
     */
    private void sendErrorToUser(String sessionId, String errorCode, String message) {
        if (sessionId == null) return;

        ErrorPayloadDto errorPayload = new ErrorPayloadDto(errorCode, message);
        WebSocketMessageDto<ErrorPayloadDto> errorMessage =
                new WebSocketMessageDto<>(WebSocketMessageType.ERROR, errorPayload);

        messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", errorMessage);
    }
}