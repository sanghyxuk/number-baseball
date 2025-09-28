package com.couple.numberbaseball.dto.websocket;

/**
 * 플레이어 연결 상태 메시지 페이로드
 */
public class PlayerConnectionPayloadDto {
    private String sessionId;
    private String nickname;
    private boolean connected;
    private String connectionStatus; // "CONNECTED", "DISCONNECTED", "RECONNECTED"

    public PlayerConnectionPayloadDto() {}

    public PlayerConnectionPayloadDto(String sessionId, String nickname, boolean connected, String connectionStatus) {
        this.sessionId = sessionId;
        this.nickname = nickname;
        this.connected = connected;
        this.connectionStatus = connectionStatus;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
}