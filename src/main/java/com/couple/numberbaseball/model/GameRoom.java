package com.couple.numberbaseball.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 게임 방 정보를 담는 핵심 클래스
 * 서버 메모리에 저장되어 모든 게임 상태를 관리
 */
public class GameRoom {
    private String roomId;              // 방 코드 (6자리 영문/숫자)
    private String creatorSessionId;    // 방장 세션 ID
    private String joinerSessionId;     // 참가자 세션 ID
    private GameStatus status;          // 게임 상태
    private GameSettings settings;      // 게임 설정 (자릿수, 0포함, 중복허용)

    private String creatorAnswer;       // 방장의 정답 숫자
    private String joinerAnswer;        // 참가자의 정답 숫자
    private String currentTurn;         // 현재 질문할 플레이어의 세션 ID

    private List<GameTurn> history;     // 전체 질문/판정 기록
    private LocalDateTime createdAt;    // 방 생성 시간
    private LocalDateTime lastActivity; // 마지막 활동 시간

    // 준비 상태 관리
    private boolean creatorReady;       // 방장 준비 상태
    private boolean joinerReady;        // 참가자 준비 상태

    public GameRoom() {
        this.history = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.status = GameStatus.WAITING_FOR_JOINER;
    }

    public GameRoom(String roomId, String creatorSessionId, GameSettings settings) {
        this();
        this.roomId = roomId;
        this.creatorSessionId = creatorSessionId;
        this.settings = settings;
    }

    // === 게임 로직 메서드 ===

    /**
     * 참가자 입장
     */
    public boolean joinRoom(String joinerSessionId) {
        if (this.status != GameStatus.WAITING_FOR_JOINER || this.joinerSessionId != null) {
            return false;
        }
        this.joinerSessionId = joinerSessionId;
        this.status = GameStatus.WAITING_FOR_READY;
        updateActivity();
        return true;
    }

    /**
     * 플레이어 준비 상태 설정
     */
    public void setPlayerReady(String sessionId, boolean ready) {
        if (creatorSessionId.equals(sessionId)) {
            this.creatorReady = ready;
        } else if (joinerSessionId != null && joinerSessionId.equals(sessionId)) {
            this.joinerReady = ready;
        }

        // 둘 다 준비되면 정답 설정 단계로
        if (creatorReady && joinerReady && status == GameStatus.WAITING_FOR_READY) {
            this.status = GameStatus.SETTING_ANSWERS;
        }
        updateActivity();
    }

    /**
     * 플레이어 정답 설정
     */
    public void setPlayerAnswer(String sessionId, String answer) {
        if (creatorSessionId.equals(sessionId)) {
            this.creatorAnswer = answer;
        } else if (joinerSessionId != null && joinerSessionId.equals(sessionId)) {
            this.joinerAnswer = answer;
        }

        // 둘 다 정답 설정하면 게임 시작
        if (creatorAnswer != null && joinerAnswer != null && status == GameStatus.SETTING_ANSWERS) {
            this.status = GameStatus.IN_PROGRESS;
            this.currentTurn = creatorSessionId; // 방장이 선공
        }
        updateActivity();
    }

    /**
     * 게임 턴 추가
     */
    public void addTurn(String guesserSessionId, String guess, String result) {
        int turnNumber = history.size() + 1;
        GameTurn turn = new GameTurn(turnNumber, guesserSessionId, guess, result);
        history.add(turn);

        // 승리 확인
        if (turn.isWinningTurn(settings.getDigits())) {
            this.status = GameStatus.FINISHED;
            this.currentTurn = null;
        } else {
            // 턴 교체
            switchTurn();
        }
        updateActivity();
    }

    /**
     * 턴 교체
     */
    private void switchTurn() {
        if (currentTurn.equals(creatorSessionId)) {
            currentTurn = joinerSessionId;
        } else {
            currentTurn = creatorSessionId;
        }
    }

    /**
     * 게임 포기
     */
    public void abandonGame(String sessionId) {
        this.status = GameStatus.ABANDONED;
        this.currentTurn = null;
        updateActivity();
    }

    /**
     * 플레이어가 이 방에 속해있는지 확인
     */
    public boolean isPlayerInRoom(String sessionId) {
        return creatorSessionId.equals(sessionId) ||
                (joinerSessionId != null && joinerSessionId.equals(sessionId));
    }

    /**
     * 현재 턴인지 확인
     */
    public boolean isPlayerTurn(String sessionId) {
        return sessionId.equals(currentTurn);
    }

    /**
     * 마지막 활동 시간 업데이트
     */
    private void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    // === Getters and Setters ===

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getCreatorSessionId() {
        return creatorSessionId;
    }

    public void setCreatorSessionId(String creatorSessionId) {
        this.creatorSessionId = creatorSessionId;
    }

    public String getJoinerSessionId() {
        return joinerSessionId;
    }

    public void setJoinerSessionId(String joinerSessionId) {
        this.joinerSessionId = joinerSessionId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    public String getCreatorAnswer() {
        return creatorAnswer;
    }

    public void setCreatorAnswer(String creatorAnswer) {
        this.creatorAnswer = creatorAnswer;
    }

    public String getJoinerAnswer() {
        return joinerAnswer;
    }

    public void setJoinerAnswer(String joinerAnswer) {
        this.joinerAnswer = joinerAnswer;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public List<GameTurn> getHistory() {
        return new ArrayList<>(history); // 방어적 복사
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public boolean isCreatorReady() {
        return creatorReady;
    }

    public boolean isJoinerReady() {
        return joinerReady;
    }

    @Override
    public String toString() {
        return String.format("GameRoom{roomId='%s', status=%s, players=%d, turns=%d}",
                roomId, status, (joinerSessionId != null ? 2 : 1), history.size());
    }
}