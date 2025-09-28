package com.couple.numberbaseball.dto.websocket;

import com.couple.numberbaseball.model.GameTurn;

/**
 * 새로운 추측 결과 메시지 페이로드
 */
public class NewGuessPayloadDto {
    private String guesser;         // 추측한 플레이어 세션 ID
    private String guess;           // 추측한 숫자
    private String result;          // 판정 결과 (예: "1S 2B")
    private int turnNumber;         // 턴 번호
    private String nextTurn;        // 다음 턴 플레이어 세션 ID

    public NewGuessPayloadDto() {}

    public NewGuessPayloadDto(String guesser, String guess, String result, int turnNumber, String nextTurn) {
        this.guesser = guesser;
        this.guess = guess;
        this.result = result;
        this.turnNumber = turnNumber;
        this.nextTurn = nextTurn;
    }

    /**
     * GameTurn 객체로부터 생성하는 정적 팩토리 메서드
     */
    public static NewGuessPayloadDto fromGameTurn(GameTurn turn, String nextTurn) {
        return new NewGuessPayloadDto(
                turn.getGuesserSessionId(),
                turn.getGuess(),
                turn.getResult(),
                turn.getTurnNumber(),
                nextTurn
        );
    }

    // Getters and Setters
    public String getGuesser() {
        return guesser;
    }

    public void setGuesser(String guesser) {
        this.guesser = guesser;
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

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public String getNextTurn() {
        return nextTurn;
    }

    public void setNextTurn(String nextTurn) {
        this.nextTurn = nextTurn;
    }
}