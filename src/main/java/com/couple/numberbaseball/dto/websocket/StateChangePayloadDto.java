package com.couple.numberbaseball.dto.websocket;

import com.couple.numberbaseball.model.GameStatus;

/**
 * 게임 상태 변경 메시지 페이로드
 */
public class StateChangePayloadDto {
    private GameStatus status;
    private String currentTurn;     // 현재 턴인 플레이어 세션 ID
    private String roomCode;
    private boolean creatorReady;
    private boolean joinerReady;

    public StateChangePayloadDto() {}

    public StateChangePayloadDto(GameStatus status, String currentTurn, String roomCode,
                                 boolean creatorReady, boolean joinerReady) {
        this.status = status;
        this.currentTurn = currentTurn;
        this.roomCode = roomCode;
        this.creatorReady = creatorReady;
        this.joinerReady = joinerReady;
    }

    // Getters and Setters
    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public boolean isCreatorReady() {
        return creatorReady;
    }

    public void setCreatorReady(boolean creatorReady) {
        this.creatorReady = creatorReady;
    }

    public boolean isJoinerReady() {
        return joinerReady;
    }

    public void setJoinerReady(boolean joinerReady) {
        this.joinerReady = joinerReady;
    }
}