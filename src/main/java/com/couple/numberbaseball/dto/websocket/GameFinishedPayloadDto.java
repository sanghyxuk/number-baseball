package com.couple.numberbaseball.dto.websocket;

import com.couple.numberbaseball.model.GameTurn;
import java.util.List;

/**
 * 게임 종료 메시지 페이로드
 */
public class GameFinishedPayloadDto {
    private String winner;          // 승리한 플레이어 세션 ID
    private String reason;          // 종료 사유 ("WIN", "ABANDON", "TIMEOUT" 등)
    private List<GameTurn> gameHistory; // 전체 게임 기록
    private int totalTurns;         // 총 턴 수

    public GameFinishedPayloadDto() {}

    public GameFinishedPayloadDto(String winner, String reason, List<GameTurn> gameHistory) {
        this.winner = winner;
        this.reason = reason;
        this.gameHistory = gameHistory;
        this.totalTurns = gameHistory != null ? gameHistory.size() : 0;
    }

    // Getters and Setters
    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<GameTurn> getGameHistory() {
        return gameHistory;
    }

    public void setGameHistory(List<GameTurn> gameHistory) {
        this.gameHistory = gameHistory;
        this.totalTurns = gameHistory != null ? gameHistory.size() : 0;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }
}