package com.couple.numberbaseball.dto;

import com.couple.numberbaseball.model.GameSettings;

/**
 * 방 생성 응답 DTO
 */
public class CreateRoomResponseDto {
    private String roomCode;         // 생성된 방 코드
    private String sessionId;       // 발급된 세션 ID
    private GameSettings settings;   // 게임 설정

    public CreateRoomResponseDto() {}

    public CreateRoomResponseDto(String roomCode, String sessionId, GameSettings settings) {
        this.roomCode = roomCode;
        this.sessionId = sessionId;
        this.settings = settings;
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
}