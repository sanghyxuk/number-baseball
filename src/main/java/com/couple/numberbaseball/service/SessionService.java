package com.couple.numberbaseball.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 관리 서비스
 * 플레이어 세션 ID 생성 및 관리
 */
@Service
public class SessionService {

    // 세션 ID -> 플레이어 정보 매핑
    private final ConcurrentHashMap<String, PlayerSession> sessions = new ConcurrentHashMap<>();

    /**
     * 새 세션 생성
     * @param nickname 플레이어 닉네임 (선택사항)
     * @return 생성된 세션 ID
     */
    public String createSession(String nickname) {
        String sessionId = generateSessionId();
        PlayerSession playerSession = new PlayerSession(sessionId, nickname);
        sessions.put(sessionId, playerSession);
        return sessionId;
    }

    /**
     * 세션 정보 조회
     * @param sessionId 세션 ID
     * @return 플레이어 세션 정보, 없으면 null
     */
    public PlayerSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 세션 존재 여부 확인
     * @param sessionId 세션 ID
     * @return 존재하면 true
     */
    public boolean isValidSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * 세션 삭제
     * @param sessionId 세션 ID
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * 플레이어 닉네임 조회
     * @param sessionId 세션 ID
     * @return 닉네임, 없으면 "Player"
     */
    public String getPlayerNickname(String sessionId) {
        PlayerSession session = sessions.get(sessionId);
        if (session != null && session.getNickname() != null) {
            return session.getNickname();
        }
        return "Player"; // 기본 닉네임
    }

    /**
     * 유니크한 세션 ID 생성
     * @return 생성된 세션 ID
     */
    private String generateSessionId() {
        return "session-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 현재 활성 세션 수 조회
     * @return 활성 세션 수
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * 세션 정리 (필요시)
     */
    public void cleanupSessions() {
        // 현재는 단순하게 모든 세션을 유지
        // 추후 TTL이나 마지막 활동 시간 기반으로 정리 로직 추가 가능
    }

    /**
     * 플레이어 세션 정보 클래스
     */
    public static class PlayerSession {
        private final String sessionId;
        private final String nickname;
        private final long createdAt;

        public PlayerSession(String sessionId, String nickname) {
            this.sessionId = sessionId;
            this.nickname = nickname;
            this.createdAt = System.currentTimeMillis();
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getNickname() {
            return nickname;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        @Override
        public String toString() {
            return String.format("PlayerSession{sessionId='%s', nickname='%s'}",
                    sessionId, nickname);
        }
    }
}