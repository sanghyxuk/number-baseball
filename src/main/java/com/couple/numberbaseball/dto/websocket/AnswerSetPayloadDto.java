package com.couple.numberbaseball.dto.websocket;

/**
 * 정답 설정 완료 메시지 페이로드
 */
public class AnswerSetPayloadDto {
    private String sessionId;
    private boolean answerSet;      // 정답 설정 완료 여부
    private boolean allAnswersSet;  // 모든 플레이어 정답 설정 완료 여부

    public AnswerSetPayloadDto() {}

    public AnswerSetPayloadDto(String sessionId, boolean answerSet, boolean allAnswersSet) {
        this.sessionId = sessionId;
        this.answerSet = answerSet;
        this.allAnswersSet = allAnswersSet;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAnswerSet() {
        return answerSet;
    }

    public void setAnswerSet(boolean answerSet) {
        this.answerSet = answerSet;
    }

    public boolean isAllAnswersSet() {
        return allAnswersSet;
    }

    public void setAllAnswersSet(boolean allAnswersSet) {
        this.allAnswersSet = allAnswersSet;
    }
}