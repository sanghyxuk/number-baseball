package com.couple.numberbaseball.dto;

import com.couple.numberbaseball.model.GameStatus;

/**
 * 게임 상태 정보 DTO (WebSocket으로 전송)
 */
public class GameStateInfoDto {
    private String roomCode;
    private GameStatus status;
    private String currentTurn;      // 현재 턴인 플레이어 세션 ID
    private boolean creatorReady;
    private boolean joinerReady;
    private int turnCount;           // 총 턴 수

    public GameStateInfoDto() {}

    // Getters and Setters
    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

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

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }
}