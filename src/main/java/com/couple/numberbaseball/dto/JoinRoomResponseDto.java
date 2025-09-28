package com.couple.numberbaseball.dto;

import com.couple.numberbaseball.model.GameSettings;
import com.couple.numberbaseball.model.GameStatus;

/**
 * 방 참가 응답 DTO
 */
public class JoinRoomResponseDto {
    private String roomCode;         // 방 코드
    private String sessionId;       // 발급된 세션 ID
    private GameSettings settings;   // 게임 설정
    private GameStatus status;       // 현재 게임 상태

    public JoinRoomResponseDto() {}

    public JoinRoomResponseDto(String roomCode, String sessionId, GameSettings settings, GameStatus status) {
        this.roomCode = roomCode;
        this.sessionId = sessionId;
        this.settings = settings;
        this.status = status;
    }

    // Getters and Setters
    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}