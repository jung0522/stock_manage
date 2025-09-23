package io.goorm.board.config;

import io.goorm.board.security.CustomAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/buyer/**").hasRole("BUYER")
                .requestMatchers("/", "/posts", "/auth/signup", "/auth/login").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.*").permitAll()
                .requestMatchers("/posts/[0-9]+").permitAll()
                .requestMatchers("/posts/new", "/posts/*/edit", "/posts/*/delete").authenticated()
                .requestMatchers("/auth/profile").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("Access denied for user: {} to URL: {}",
                        request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                        request.getRequestURI());
                    response.sendRedirect("/error/403");
                })
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
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