package com.couple.numberbaseball.interceptor;

import com.couple.numberbaseball.service.GameRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final GameRoomService gameRoomService;

    // @Lazy 어노테이션을 사용하여 순환 참조를 해결합니다.
    @Autowired
    public WebSocketChannelInterceptor(@Lazy GameRoomService gameRoomService) {
        this.gameRoomService = gameRoomService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // CONNECT 또는 SEND 요청일 때 세션 유효성 검사
        if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            String sessionId = accessor.getFirstNativeHeader("X-Player-Session-Id");

            if (sessionId == null || gameRoomService.findRoomBySessionId(sessionId) == null) {
                // 유효하지 않은 세션 ID일 경우 연결을 거부하거나 에러를 처리할 수 있습니다.
                // 여기서는 간단히 로그만 남깁니다.
                System.out.println("Invalid or missing session ID: " + sessionId);
                // throw new IllegalArgumentException("Invalid session ID");
            }
        }

        return message;
    }
}
