package io.goorm.board.service.impl;

import io.goorm.board.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 업로드 서비스 구현체
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.upload.root:src/main/resources/static/uploads}")
    private String uploadRoot;

    @Value("${app.upload.url:/uploads}")
    private String uploadUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        try {
            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get(uploadRoot);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 웹 접근 URL 반환
            String webUrl = uploadUrl + "/" + uniqueFilename;
            log.info("파일 업로드 성공: {} -> {}", originalFilename, webUrl);
            
            return webUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }

        try {
            // URL에서 파일명 추출
            String filename = extractFilenameFromUrl(fileUrl);
            if (filename == null) {
                log.warn("파일명을 추출할 수 없습니다: {}", fileUrl);
                return;
            }

            // 파일 삭제
            Path filePath = Paths.get(uploadRoot, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 성공: {}", filename);
            } else {
                log.warn("삭제할 파일이 존재하지 않습니다: {}", filename);
            }

        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 파일 크기 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기가 너무 큽니다: {} bytes", file.getSize());
            return false;
        }

        // MIME 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            log.warn("지원하지 않는 파일 형식입니다: {}", contentType);
            return false;
        }

        // 파일 확장자 검사
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return false;
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
        
        return allowedExtensions.contains(extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFilenameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }
        
        // URL에서 마지막 '/' 이후의 부분을 파일명으로 사용
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == fileUrl.length() - 1) {
            return null;
        }
        
        return fileUrl.substring(lastSlashIndex + 1);
    }
}
