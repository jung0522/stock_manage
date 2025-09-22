package io.goorm.board.security;

import io.goorm.board.entity.User;
import io.goorm.board.enums.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        User user = (User) authentication.getPrincipal();
        UserRole role = user.getRole();
        
        log.info("로그인 성공: email={}, role={}", user.getEmail(), role.getDisplayName());
        
        // 역할에 따른 리다이렉트
        if (role == UserRole.ADMIN) {
            response.sendRedirect("/admin/dashboard");
        } else if (role == UserRole.BUYER) {
            response.sendRedirect("/buyer/dashboard");
        } else {
            // 기본값 (예외 상황)
            response.sendRedirect("/");
        }
    }
}
