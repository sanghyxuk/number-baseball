package com.couple.numberbaseball.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateChangePayload {
    private GameStatus status;
    private String currentTurn;
    private String roomCode;
    private boolean creatorReady;
    private boolean joinerReady;

    // 기본 생성자 (필요한 경우)
    public StateChangePayload() {
    }

    // 모든 필드를 인자로 받는 생성자 (오류 해결의 핵심)
    public StateChangePayload(GameStatus status, String currentTurn, String roomCode, boolean creatorReady, boolean joinerReady) {
        this.status = status;
        this.currentTurn = currentTurn;
        this.roomCode = roomCode;
        this.creatorReady = creatorReady;
        this.joinerReady = joinerReady;
    }
}

