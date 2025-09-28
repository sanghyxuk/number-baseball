package com.couple.numberbaseball.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * WebSocket 및 HTTP 보안 설정
 * 게임 전용 단순 보안 구성
 */
@Configuration
@EnableWebSecurity
public class WebSocketSecurityConfig {

    /**
     * HTTP 보안 설정
     * REST API와 WebSocket 엔드포인트에 대한 기본 보안 구성
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API와 WebSocket에서는 불필요)
                .csrf(csrf -> csrf.disable())

                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 관리 설정 (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // HTTP 요청 인가 설정
                .authorizeHttpRequests(authz -> authz
                        // 게임 API는 모든 사용자 접근 허용 (자체 세션 관리 사용)
                        .requestMatchers("/api/game/**").permitAll()

                        // WebSocket 엔드포인트 접근 허용
                        .requestMatchers("/ws/**", "/ws-native/**").permitAll()

                        // 정적 리소스 접근 허용
                        .requestMatchers("/static/**", "/public/**").permitAll()

                        // 헬스체크 엔드포인트 접근 허용
                        .requestMatchers("/actuator/health").permitAll()

                        // 기타 모든 요청은 인증 필요 (현재는 사용하지 않음)
                        .anyRequest().authenticated()
                )

                // 기본 폼 로그인 비활성화
                .formLogin(form -> form.disable())

                // HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * CORS 설정
     * 프론트엔드에서 WebSocket 및 REST API 접근을 위한 CORS 구성
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발용 설정 (실제 배포시에는 특정 도메인으로 제한)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // 노출할 헤더 (WebSocket 연결시 필요)
        configuration.setExposedHeaders(Arrays.asList(
                "X-Player-Session-Id", "Access-Control-Allow-Origin"
        ));

        // Preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}