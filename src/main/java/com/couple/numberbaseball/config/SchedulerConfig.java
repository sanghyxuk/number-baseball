package com.couple.numberbaseball.config;

import com.couple.numberbaseball.service.GameRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 스케줄러 설정
 * 비활성화된 방들을 주기적으로 정리
 */
@Component
public class SchedulerConfig {

    @Autowired
    private GameRoomService gameRoomService;

    /**
     * 5분마다 비활성화된 방들 정리
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void cleanupInactiveRooms() {
        try {
            gameRoomService.cleanupInactiveRooms();
        } catch (Exception e) {
            System.err.println("방 정리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 서버 상태 로그 (10분마다)
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void logServerStatus() {
        try {
            int activeRooms = gameRoomService.getActiveRoomCount();
            System.out.println(String.format("[서버 상태] 활성 방: %d개", activeRooms));
        } catch (Exception e) {
            System.err.println("서버 상태 로그 출력 중 오류: " + e.getMessage());
        }
    }
}