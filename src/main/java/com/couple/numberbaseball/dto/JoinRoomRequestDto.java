package com.couple.numberbaseball.dto;

/**
 * 방 참가 요청 DTO
 */
public class JoinRoomRequestDto {
    private String roomCode;         // 방 코드
    private String nickname;         // 플레이어 닉네임 (선택사항)

    public JoinRoomRequestDto() {}

    // Getters and Setters
    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}