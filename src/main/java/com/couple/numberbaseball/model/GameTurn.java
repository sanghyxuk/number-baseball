package com.couple.numberbaseball.model;

import java.time.LocalDateTime;

/**
 * 게임 턴 정보
 * - 턴 번호, 추측한 플레이어, 추측 숫자, 판정 결과
 */
public class GameTurn {
    private int turnNumber;        // 턴 번호 (1부터 시작)
    private String guesserSessionId; // 추측한 플레이어의 세션 ID
    private String guess;          // 추측한 숫자
    private String result;         // 판정 결과 (예: "1S 2B", "3S")
    private LocalDateTime timestamp; // 턴이 진행된 시간

    public GameTurn() {}

    public GameTurn(int turnNumber, String guesserSessionId, String guess, String result) {
        this.turnNumber = turnNumber;
        this.guesserSessionId = guesserSessionId;
        this.guess = guess;
        this.result = result;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public String getGuesserSessionId() {
        return guesserSessionId;
    }

    public void setGuesserSessionId(String guesserSessionId) {
        this.guesserSessionId = guesserSessionId;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 이 턴이 완전한 스트라이크(승리)인지 확인
     */
    public boolean isWinningTurn(int totalDigits) {
        return result != null && result.equals(totalDigits + "S");
    }

    @Override
    public String toString() {
        return String.format("GameTurn{turnNumber=%d, guesser='%s', guess='%s', result='%s', timestamp=%s}",
                turnNumber, guesserSessionId, guess, result, timestamp);
    }
}