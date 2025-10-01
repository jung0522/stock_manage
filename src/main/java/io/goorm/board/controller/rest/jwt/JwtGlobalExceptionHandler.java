package io.goorm.board.controller.rest.jwt;

import io.goorm.board.dto.ApiResponse;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice(basePackages = "io.goorm.board.controller.rest.jwt")
public class JwtGlobalExceptionHandler {
    
    private final MessageSource messageSource;
    
    public JwtGlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException e) {
        log.error("JWT 예외 발생: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("JWT 토큰 오류: " + e.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("JWT 토큰 만료: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("토큰이 만료되었습니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJwtException(MalformedJwtException e) {
        log.error("잘못된 JWT 토큰 형식: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("잘못된 토큰 형식입니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<Object>> handleSignatureException(SignatureException e) {
        log.error("JWT 서명 검증 실패: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("토큰 서명이 유효하지 않습니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedJwtException(UnsupportedJwtException e) {
        log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("지원되지 않는 토큰입니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 인자: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("잘못된 요청입니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 검증 실패 예외 처리
     * @Valid 어노테이션으로 인한 검증 실패 시 JSON 응답 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("검증 실패: {}", e.getMessage());

        Map<String, Object> validationErrors = new HashMap<>();
        
        // 각 필드의 검증 오류를 수집
        e.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            
            // 메시지 키가 있는 경우 국제화된 메시지로 변환
            if (errorMessage != null && errorMessage.startsWith("{")) {
                try {
                    String messageKey = errorMessage.substring(1, errorMessage.length() - 1);
                    errorMessage = messageSource.getMessage(
                        messageKey, 
                        error.getArguments(), 
                        errorMessage, 
                        LocaleContextHolder.getLocale()
                    );
                } catch (Exception ex) {
                    // 메시지 변환 실패 시 기본 메시지 사용
                    log.debug("메시지 변환 실패: {}", errorMessage);
                }
            }
            
            validationErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> data = Map.of(
            "validationErrors", validationErrors,
            "errorCount", e.getBindingResult().getErrorCount()
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("입력 데이터 검증에 실패했습니다.")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}