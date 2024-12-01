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

        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        http.headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy((SessionCreationPolicy.STATELESS)));

        http.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
        );

        http.apply(new JwtSecurityConfig(tokenProvider));

        return http.build();
    }
}
