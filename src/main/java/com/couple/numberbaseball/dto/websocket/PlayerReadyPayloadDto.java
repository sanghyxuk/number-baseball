package com.couple.numberbaseball.dto.websocket;

/**
 * 플레이어 준비 상태 메시지 페이로드
 */
public class PlayerReadyPayloadDto {
    private String sessionId;
    private boolean ready;
    private String nickname;

    public PlayerReadyPayloadDto() {}

    public PlayerReadyPayloadDto(String sessionId, boolean ready, String nickname) {
        this.sessionId = sessionId;
        this.ready = ready;
        this.nickname = nickname;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}