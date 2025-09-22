package io.goorm.board.config;

import io.goorm.board.security.CustomAuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    
    public SecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/posts", "/auth/signup", "/auth/login").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.*").permitAll()  // 정적 리소스 접근 허용
                .requestMatchers("/posts/[0-9]+").permitAll()  // 숫자로 된 게시글 조회만 허용
                .requestMatchers("/posts/new", "/posts/*/edit", "/posts/*/delete").authenticated()
                .requestMatchers("/auth/profile").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")  // 관리자만 접근 가능
                .requestMatchers("/buyer/**").hasRole("BUYER")  // 바이어만 접근 가능
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .usernameParameter("email")  // email 파라미터를 username으로 사용
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler)  // 커스텀 핸들러 사용
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}