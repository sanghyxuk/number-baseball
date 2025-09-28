package com.couple.numberbaseball.model;

/**
 * 게임 상태 enum
 * 계획서에 따른 세분화된 상태 관리
 */
public enum GameStatus {
    WAITING_FOR_JOINER("참가자 대기 중"),
    WAITING_FOR_READY("게임 시작 준비 대기 중"),
    SETTING_ANSWERS("정답 설정 중"),
    IN_PROGRESS("게임 진행 중"),
    FINISHED("게임 종료"),
    ABANDONED("게임 포기됨");

    private final String description;

    GameStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 게임이 활성 상태인지 확인
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == SETTING_ANSWERS;
    }

    /**
     * 게임이 종료 상태인지 확인
     */
    public boolean isFinished() {
        return this == FINISHED || this == ABANDONED;
    }

    /**
     * 플레이어가 참가할 수 있는 상태인지 확인
     */
    public boolean canJoin() {
        return this == WAITING_FOR_JOINER;
    }
}