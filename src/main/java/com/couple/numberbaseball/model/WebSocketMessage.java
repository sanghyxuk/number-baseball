package com.couple.numberbaseball.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketMessage<T> {
    private WebSocketMessageType type;
    private T payload;
    private final long timestamp;

    public WebSocketMessage(WebSocketMessageType type, T payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
}
