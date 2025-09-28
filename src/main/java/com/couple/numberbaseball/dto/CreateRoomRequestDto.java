package com.couple.numberbaseball.dto;

import com.couple.numberbaseball.model.GameSettings;

/**
 * 방 생성 요청 DTO
 */
public class CreateRoomRequestDto {
    private String nickname;          // 플레이어 닉네임 (선택사항)
    private int digits;              // 자릿수 (3, 4, 5)
    private boolean allowZero;       // 0 포함 여부
    private boolean allowDuplicate;  // 중복 허용 여부

    // 기본 생성자
    public CreateRoomRequestDto() {}

    // Getters and Setters
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

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

    /**
     * GameSettings 객체로 변환
     */
    public GameSettings toGameSettings() {
        return new GameSettings(digits, allowZero, allowDuplicate);
    }
}