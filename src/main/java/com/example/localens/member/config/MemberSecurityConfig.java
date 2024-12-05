package com.example.localens.member.config;


import com.example.localens.member.jwt.JwtAuthenticationEntryPoint;
import com.example.localens.member.jwt.JwtSecurityConfig;
import com.example.localens.member.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class MemberSecurityConfig {
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 설정 Disable
        http.csrf(csrf -> csrf.disable());

        // CORS 필터 추가
        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);

        // Exception handling 설정
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        // H2 콘솔을 위한 설정
        http.headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // 세션 관리 설정 -> STATELESS (JWT 사용 시 필요)
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 요청에 대한 권한 설정
        http.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/**",
                                "/api/member/**",
                                "/api/datecompare/**",
                                "/api/details/**",
                                "/api/main/**",
                                "/api/compare/**",
                                "/api/improvements/**"
                        ).permitAll() // 인증 없이 접근을 허용하는 엔드포인트
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
        );

        // JWT 관련 설정 추가
        http.apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }
}
