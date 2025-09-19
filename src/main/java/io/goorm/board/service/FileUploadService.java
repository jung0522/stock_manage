package io.goorm.board.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 서비스 인터페이스
 */
public interface FileUploadService {
    
    /**
     * 이미지 파일 업로드
     * @param file 업로드할 파일
     * @return 업로드된 파일의 웹 접근 URL
     */
    String uploadImage(MultipartFile file);
    
    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 파일 유효성 검사
     * @param file 검사할 파일
     * @return 유효한 파일인지 여부
     */
    boolean isValidImageFile(MultipartFile file);
}
