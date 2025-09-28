package com.couple.numberbaseball.dto.websocket;

/**
 * 기본 WebSocket 메시지 구조
 * 계획서의 통신 규약에 따른 일관된 JSON 구조
 */
public class WebSocketMessageDto<T> {
    private WebSocketMessageType type;
    private T payload;
    private long timestamp;

    public WebSocketMessageDto() {
        this.timestamp = System.currentTimeMillis();
    }

    public WebSocketMessageDto(WebSocketMessageType type, T payload) {
        this();
        this.type = type;
        this.payload = payload;
    }

    // Getters and Setters
    public WebSocketMessageType getType() {
        return type;
    }

    public void setType(WebSocketMessageType type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}