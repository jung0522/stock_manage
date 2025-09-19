package io.goorm.board.config;

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
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/posts", "/products", "/auth/signup", "/auth/login").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.*").permitAll()  // 정적 리소스 접근 허용
                .requestMatchers("/posts/new", "/posts/*/edit", "/posts/*/delete").authenticated()  // 게시판 작성/수정/삭제는 로그인 필요
                .requestMatchers("/posts/*").permitAll()  // 포스트 상세 조회는 모든 사용자 허용
                .requestMatchers("/products/new", "/products/*/edit", "/products/*/delete").authenticated()  // 상품 등록/수정/삭제는 로그인 필요
                .requestMatchers("/products/*").permitAll()  // 상품 조회는 모든 사용자 허용
                .requestMatchers("/auth/profile").authenticated()  // 프로필 페이지는 로그인 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .usernameParameter("email")  // email 파라미터를 username으로 사용
                .passwordParameter("password")
                .defaultSuccessUrl("/posts", true)  // true로 설정하여 강제 리다이렉트
                .failureUrl("/auth/login?error=true")
                .successHandler((request, response, authentication) -> {
                    log.info("로그인 성공: user={}, authorities={}", 
                            authentication.getName(), authentication.getAuthorities());
                    response.sendRedirect("/posts");
                })
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