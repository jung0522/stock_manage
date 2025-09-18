package io.goorm.board.service.impl;

import io.goorm.board.dto.product.ProductCreateDto;
import io.goorm.board.dto.product.ProductDto;
import io.goorm.board.dto.product.ProductSearchDto;
import io.goorm.board.dto.product.ProductUpdateDto;
import io.goorm.board.entity.Category;
import io.goorm.board.entity.Product;
import io.goorm.board.entity.Supplier;
import io.goorm.board.enums.ProductStatus;
import io.goorm.board.mapper.ProductMapper;
import io.goorm.board.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto create(ProductCreateDto createDto) {
        log.info("상품 등록: {}", createDto);
        
        // DTO를 Entity로 변환
        Product product = Product.builder()
                .code(createDto.getCode())
                .name(createDto.getName())
                .description(createDto.getDescription())
                .categorySeq(createDto.getCategorySeq())
                .supplierSeq(createDto.getSupplierSeq())
                .unitPrice(createDto.getUnitPrice())
                .unitCost(createDto.getUnitCost())
                .unit(createDto.getUnit())
                .sku(createDto.getSku())
                .barcode(createDto.getBarcode())
                .weight(createDto.getWeight())
                .dimensions(createDto.getDimensions())
                .status(createDto.getStatus())
                .build();
        
        // 이미지 파일 처리 (추후 구현)
        if (createDto.hasImageFile()) {
            // TODO: 이미지 파일 업로드 처리
            // product.setImageUrl(uploadImage(createDto.getImageFile()));
        }
        
        // 상품 저장
        productMapper.insert(product);
        
        // 저장된 상품 조회하여 DTO로 변환
        return findById(product.getProductSeq());
    }

    @Override
    @Transactional
    public ProductDto update(ProductUpdateDto updateDto) {
        log.info("상품 수정: {}", updateDto);
        
        // 기존 상품 조회
        Product existingProduct = productMapper.findById(updateDto.getProductSeq())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + updateDto.getProductSeq()));
        
        // 필드 업데이트
        existingProduct.setCode(updateDto.getCode());
        existingProduct.setName(updateDto.getName());
        existingProduct.setDescription(updateDto.getDescription());
        existingProduct.setCategorySeq(updateDto.getCategorySeq());
        existingProduct.setSupplierSeq(updateDto.getSupplierSeq());
        existingProduct.setUnitPrice(updateDto.getUnitPrice());
        existingProduct.setUnitCost(updateDto.getUnitCost());
        existingProduct.setUnit(updateDto.getUnit());
        existingProduct.setSku(updateDto.getSku());
        existingProduct.setBarcode(updateDto.getBarcode());
        existingProduct.setWeight(updateDto.getWeight());
        existingProduct.setDimensions(updateDto.getDimensions());
        existingProduct.setStatus(updateDto.getStatus());
        
        // 이미지 처리
        if (updateDto.isDeleteImageRequested()) {
            existingProduct.setImageUrl(null);
        } else if (updateDto.hasImageFile()) {
            // TODO: 이미지 파일 업로드 처리
            // existingProduct.setImageUrl(uploadImage(updateDto.getImageFile()));
        }
        
        // 상품 업데이트
        productMapper.update(existingProduct);
        
        // 업데이트된 상품 조회하여 DTO로 변환
        return findById(existingProduct.getProductSeq());
    }

    @Override
    @Transactional
    public void delete(Long productSeq) {
        log.info("상품 삭제: productSeq={}", productSeq);
        
        // 상품 존재 확인
        Product product = productMapper.findById(productSeq)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productSeq));
        
        // 상품 삭제
        productMapper.delete(productSeq);
    }

    @Override
    public ProductDto findById(Long productSeq) {
        log.info("상품 조회: productSeq={}", productSeq);
        
        Product product = productMapper.findById(productSeq)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productSeq));
        
        return convertToDto(product);
    }

    @Override
    public Page<ProductDto> findAll(ProductSearchDto searchDto) {
        log.info("상품 목록 조회: {}", searchDto);
        
        // 페이징 정보 설정
        Pageable pageable = Pageable.ofSize(searchDto.getSize()).withPage(searchDto.getPage());
        
        // 상품 목록 조회
        List<ProductDto> productDtos = productMapper.findAll(searchDto);
        
        // 총 개수 조회
        long totalCount = productMapper.count(searchDto);
        
        return new PageImpl<>(productDtos, pageable, totalCount);
    }

    @Override
    public boolean isCodeDuplicate(String code) {
        return productMapper.existsByCode(code);
    }

    @Override
    public boolean isCodeDuplicate(String code, Long excludeProductSeq) {
        return productMapper.existsByCodeAndNotSeq(code, excludeProductSeq);
    }

    @Override
    public List<ProductDto> findByStatus(String status) {
        log.info("상태별 상품 조회: status={}", status);
        
        return productMapper.findByStatus(status);
    }

    @Override
    public List<ProductDto> findSellableProducts() {
        log.info("판매 가능한 상품 조회");
        
        return productMapper.findSellableProducts();
    }

    @Override
    public List<ProductDto> findRecentProducts(int limit) {
        log.info("최근 등록된 상품 조회: limit={}", limit);
        
        return productMapper.findRecentProducts(limit);
    }

    @Override
    public int countByCategory(Long categorySeq) {
        return productMapper.countByCategory(categorySeq);
    }

    @Override
    public int countBySupplier(Long supplierSeq) {
        return productMapper.countBySupplier(supplierSeq);
    }

    /**
     * Product Entity를 ProductDto로 변환
     */
    private ProductDto convertToDto(Product product) {
        return ProductDto.builder()
                .productSeq(product.getProductSeq())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .categorySeq(product.getCategorySeq())
                .supplierSeq(product.getSupplierSeq())
                .unitPrice(product.getUnitPrice())
                .unitCost(product.getUnitCost())
                .unit(product.getUnit())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
