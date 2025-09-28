package com.couple.numberbaseball.model;

/**
 * 게임 설정 정보
 * - 자릿수, 0 포함 여부, 중복 허용 여부
 */
public class GameSettings {
    private int digits;           // 3, 4, 5 중 선택
    private boolean allowZero;    // 0 사용 가능 여부
    private boolean allowDuplicate; // 중복 숫자 허용 여부

    public GameSettings() {}

    public GameSettings(int digits, boolean allowZero, boolean allowDuplicate) {
        this.digits = digits;
        this.allowZero = allowZero;
        this.allowDuplicate = allowDuplicate;
    }

    // Getters and Setters
    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public boolean isAllowZero() {
        return allowZero;
    }

    public void setAllowZero(boolean allowZero) {
        this.allowZero = allowZero;
    }

    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }

    public void setAllowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    @Override
    public String toString() {
        return String.format("GameSettings{digits=%d, allowZero=%s, allowDuplicate=%s}",
                digits, allowZero, allowDuplicate);
    }
}