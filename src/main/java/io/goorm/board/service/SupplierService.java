package io.goorm.board.service;

import io.goorm.board.dto.supplier.*;
import io.goorm.board.enums.SupplierStatus;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 공급업체 서비스 인터페이스
 */
public interface SupplierService {

    /**
     * 활성 공급업체 목록 조회
     */
    List<SupplierDto> findAllActive();

    /**
     * 활성 공급업체 목록 조회 (선택된 공급업체 포함)
     */
    List<SupplierDto> findAllActiveOrSelected(Long selectedSupplierSeq);

}