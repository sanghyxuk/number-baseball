package com.couple.numberbaseball.dto.websocket;

/**
 * WebSocket 메시지 타입 enum
 */
public enum WebSocketMessageType {
    // 게임 상태 변경
    STATE_CHANGE,

    // 플레이어 준비 상태
    PLAYER_READY,

    // 정답 설정 완료 알림
    ANSWER_SET,

    // 새로운 추측/판정 결과
    NEW_GUESS,

    // 게임 종료
    GAME_FINISHED,

    // 플레이어 연결/연결 끊김
    PLAYER_CONNECTED,
    PLAYER_DISCONNECTED,

    // 오류 메시지
    ERROR
}