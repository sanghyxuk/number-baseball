package com.couple.numberbaseball.model;

public enum WebSocketMessageType {
    STATE_CHANGE,
    PLAYER_READY,
    ANSWER_SET,
    NEW_GUESS,
    GAME_FINISHED,
    PLAYER_CONNECTED,
    PLAYER_DISCONNECTED,
    ERROR
}
