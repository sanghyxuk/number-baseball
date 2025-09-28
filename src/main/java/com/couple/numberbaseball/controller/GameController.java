package com.couple.numberbaseball.controller;

import com.couple.numberbaseball.dto.*;
import com.couple.numberbaseball.model.GameRoom;
import com.couple.numberbaseball.service.GameRoomService;
import com.couple.numberbaseball.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 게임 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*") // 개발용, 실제 배포시에는 특정 도메인으로 제한
public class GameController {

    @Autowired
    private GameRoomService gameRoomService;

    @Autowired
    private SessionService sessionService;

    /**
     * 게임 방 생성 API
     * POST /api/game/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<CreateRoomResponseDto>> createRoom(
            @RequestBody CreateRoomRequestDto request) {

        try {
            // 입력 검증
            if (request.getDigits() < 3 || request.getDigits() > 5) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDto.error("자릿수는 3, 4, 5 중 하나여야 합니다."));
            }

            // 세션 생성
            String sessionId = sessionService.createSession(request.getNickname());

            // 게임 방 생성
            GameRoom room = gameRoomService.createRoom(sessionId, request.toGameSettings());

            // 응답 생성
            CreateRoomResponseDto response = new CreateRoomResponseDto(
                    room.getRoomId(),
                    sessionId,
                    room.getSettings()
            );

            return ResponseEntity.ok(ApiResponseDto.success("방이 성공적으로 생성되었습니다.", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("방 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 게임 방 참가 API
     * POST /api/game/join
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponseDto<JoinRoomResponseDto>> joinRoom(
            @RequestBody JoinRoomRequestDto request) {

        try {
            // 입력 검증
            if (request.getRoomCode() == null || request.getRoomCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDto.error("방 코드를 입력해주세요."));
            }

            String roomCode = request.getRoomCode().trim().toUpperCase();

            // 방 존재 여부 확인
            GameRoom existingRoom = gameRoomService.findRoomByCode(roomCode);
            if (existingRoom == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("존재하지 않는 방 코드입니다."));
            }

            // 방 상태 확인
            if (!existingRoom.getStatus().canJoin()) {
                String message = existingRoom.getStatus().isFinished() ?
                        "이미 종료된 방입니다." : "이미 시작되었거나 인원이 가득 찬 방입니다.";
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseDto.error(message));
            }

            // 세션 생성
            String sessionId = sessionService.createSession(request.getNickname());

            // 방 참가
            GameRoom room = gameRoomService.joinRoom(roomCode, sessionId);

            if (room == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseDto.error("방 참가에 실패했습니다."));
            }

            // 응답 생성
            JoinRoomResponseDto response = new JoinRoomResponseDto(
                    room.getRoomId(),
                    sessionId,
                    room.getSettings(),
                    room.getStatus()
            );

            return ResponseEntity.ok(ApiResponseDto.success("방에 성공적으로 참가했습니다.", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("방 참가 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 게임 상태 조회 API (선택사항)
     * GET /api/game/status/{sessionId}
     */
    @GetMapping("/status/{sessionId}")
    public ResponseEntity<ApiResponseDto<GameStateInfoDto>> getGameStatus(
            @PathVariable String sessionId) {

        try {
            // 세션 검증
            if (!sessionService.isValidSession(sessionId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("유효하지 않은 세션입니다."));
            }

            // 현재 참가중인 방 찾기
            GameRoom room = gameRoomService.findRoomBySessionId(sessionId);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("참가중인 방이 없습니다."));
            }

            // 게임 상태 정보 생성
            GameStateInfoDto stateInfo = new GameStateInfoDto();
            stateInfo.setRoomCode(room.getRoomId());
            stateInfo.setStatus(room.getStatus());
            stateInfo.setCurrentTurn(room.getCurrentTurn());
            stateInfo.setCreatorReady(room.isCreatorReady());
            stateInfo.setJoinerReady(room.isJoinerReady());
            stateInfo.setTurnCount(room.getHistory().size());

            return ResponseEntity.ok(ApiResponseDto.success(stateInfo));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("상태 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 방 나가기 API (선택사항)
     * POST /api/game/leave/{sessionId}
     */
    @PostMapping("/leave/{sessionId}")
    public ResponseEntity<ApiResponseDto<Void>> leaveRoom(@PathVariable String sessionId) {

        try {
            // 세션 검증
            if (!sessionService.isValidSession(sessionId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDto.error("유효하지 않은 세션입니다."));
            }

            // 방 나가기
            gameRoomService.leaveRoom(sessionId);

            // 세션 정리
            sessionService.removeSession(sessionId);

            return ResponseEntity.ok(ApiResponseDto.success("방을 나갔습니다.", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("방 나가기 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 서버 상태 확인 API (디버깅용)
     * GET /api/game/debug/status
     */
    @GetMapping("/debug/status")
    public ResponseEntity<ApiResponseDto<String>> getServerStatus() {
        try {
            int activeRooms = gameRoomService.getActiveRoomCount();
            int activeSessions = sessionService.getActiveSessionCount();

            String status = String.format("활성 방: %d개, 활성 세션: %d개", activeRooms, activeSessions);
            return ResponseEntity.ok(ApiResponseDto.success(status));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("서버 상태 조회 실패: " + e.getMessage()));
        }
    }
}