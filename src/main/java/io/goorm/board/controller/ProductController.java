package io.goorm.board.controller;

import io.goorm.board.dto.CategoryDto;
import io.goorm.board.dto.SupplierDto;
import io.goorm.board.dto.product.ProductCreateDto;
import io.goorm.board.dto.product.ProductDto;
import io.goorm.board.dto.product.ProductSearchDto;
import io.goorm.board.dto.product.ProductUpdateDto;
import io.goorm.board.entity.Category;
import io.goorm.board.entity.Supplier;
import io.goorm.board.enums.ProductStatus;
import io.goorm.board.service.CategoryService;
import io.goorm.board.service.ProductService;
import io.goorm.board.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 관리 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    /**
     * 카테고리 목록 조회 헬퍼 메서드
     */
    private List<Category> getCategories() {
        List<CategoryDto> categoryDtos = categoryService.findAllActive();
        return categoryDtos.stream()
                .map(dto -> Category.builder()
                        .categorySeq(dto.getCategorySeq())
                        .name(dto.getName())
                        .description(dto.getDescription())
                        .parentCategorySeq(dto.getParentCategorySeq())
                        .sortOrder(dto.getSortOrder())
                        .isActive(dto.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 공급업체 목록 조회 헬퍼 메서드
     */
    private List<Supplier> getSuppliers() {
        List<SupplierDto> supplierDtos = supplierService.findAllActive();
        return supplierDtos.stream()
                .map(dto -> Supplier.builder()
                        .supplierSeq(dto.getSupplierSeq())
                        .name(dto.getName())
                        .contactPerson(dto.getContactPerson())
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
                        .address(dto.getAddress())
                        .description(dto.getDescription())
                        .isActive(dto.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 상품 목록 조회
     */
    @GetMapping
    public String list(ProductSearchDto searchDto, Model model) {
        log.info("상품 목록 조회 요청: {}", searchDto);
        
        try {
            // 상품 목록 조회
            Page<ProductDto> products = productService.findAll(searchDto);
            
            // 카테고리 목록 조회
            List<Category> categories = getCategories();
            
            // 모델에 데이터 추가
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("productStatuses", ProductStatus.values());
            model.addAttribute("search", searchDto);
            model.addAttribute("totalElements", products.getTotalElements());
            
            return "product/list";
        } catch (Exception e) {
            log.error("상품 목록 조회 오류: {}", e.getMessage(), e);
            model.addAttribute("error", "상품 목록을 불러오는 중 오류가 발생했습니다.");
            return "product/list";
        }
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productSeq}")
    public String show(@PathVariable Long productSeq, Model model) {
        log.info("상품 상세 조회 요청: productSeq={}", productSeq);
        
        try {
            ProductDto product = productService.findById(productSeq);
            model.addAttribute("product", product);
            return "product/show";
        } catch (Exception e) {
            log.error("상품 상세 조회 오류: productSeq={}, error={}", productSeq, e.getMessage(), e);
            model.addAttribute("error", "상품을 찾을 수 없습니다.");
            return "error/404";
        }
    }

    /**
     * 상품 등록 폼
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        log.info("상품 등록 폼 요청");
        
        try {
            // 카테고리 목록 조회
            List<Category> categories = getCategories();
            
            // 공급업체 목록 조회
            List<Supplier> suppliers = getSuppliers();
            
            model.addAttribute("product", new ProductCreateDto());
            model.addAttribute("categories", categories);
            model.addAttribute("suppliers", suppliers);
            model.addAttribute("productStatuses", ProductStatus.values());
            
            return "product/form";
        } catch (Exception e) {
            log.error("상품 등록 폼 오류: {}", e.getMessage(), e);
            model.addAttribute("error", "상품 등록 폼을 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 상품 등록 처리
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("product") ProductCreateDto createDto, 
                        BindingResult bindingResult, 
                        Model model, 
                        RedirectAttributes redirectAttributes) {
        log.info("상품 등록 요청: {}", createDto);
        
        try {
            if (bindingResult.hasErrors()) {
                log.warn("상품 등록 검증 오류: {}", bindingResult.getAllErrors());
                
                // 카테고리와 공급업체 목록 다시 조회
                List<Category> categories = getCategories();
                List<Supplier> suppliers = getSuppliers();
                
                model.addAttribute("categories", categories);
                model.addAttribute("suppliers", suppliers);
                model.addAttribute("productStatuses", ProductStatus.values());
                
                return "product/form";
            }
            
            // 상품 코드 중복 확인
            if (productService.isCodeDuplicate(createDto.getCode())) {
                bindingResult.rejectValue("code", "duplicate", "이미 사용 중인 상품 코드입니다.");
                
                List<Category> categories = getCategories();
                List<Supplier> suppliers = getSuppliers();
                
                model.addAttribute("categories", categories);
                model.addAttribute("suppliers", suppliers);
                model.addAttribute("productStatuses", ProductStatus.values());
                
                return "product/form";
            }
            
            ProductDto savedProduct = productService.create(createDto);
            log.info("상품 등록 성공: productSeq={}", savedProduct.getProductSeq());
            
            redirectAttributes.addFlashAttribute("success", "상품이 성공적으로 등록되었습니다.");
            return "redirect:/products/" + savedProduct.getProductSeq();
            
        } catch (Exception e) {
            log.error("상품 등록 오류: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "상품 등록 중 오류가 발생했습니다.");
            return "redirect:/products";
        }
    }

    /**
     * 상품 수정 폼
     */
    @GetMapping("/{productSeq}/edit")
    public String editForm(@PathVariable Long productSeq, Model model) {
        log.info("상품 수정 폼 요청: productSeq={}", productSeq);
        
        try {
            ProductDto product = productService.findById(productSeq);
            List<Category> categories = getCategories();
            List<Supplier> suppliers = getSuppliers();
            
            // ProductDto를 ProductUpdateDto로 변환
            ProductUpdateDto updateDto = ProductUpdateDto.builder()
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
                    .currentImageUrl(product.getImageUrl())
                    .status(product.getStatus())
                    .build();
            
            model.addAttribute("product", updateDto);
            model.addAttribute("categories", categories);
            model.addAttribute("suppliers", suppliers);
            model.addAttribute("productStatuses", ProductStatus.values());
            
            return "product/form";
        } catch (Exception e) {
            log.error("상품 수정 폼 오류: productSeq={}, error={}", productSeq, e.getMessage(), e);
            model.addAttribute("error", "상품을 찾을 수 없습니다.");
            return "error/404";
        }
    }

    /**
     * 상품 수정 처리
     */
    @PostMapping("/{productSeq}/edit")
    public String update(@PathVariable Long productSeq,
                        @Valid @ModelAttribute("product") ProductUpdateDto updateDto,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        log.info("상품 수정 요청: productSeq={}, {}", productSeq, updateDto);
        
        try {
            if (bindingResult.hasErrors()) {
                log.warn("상품 수정 검증 오류: {}", bindingResult.getAllErrors());
                
                List<Category> categories = getCategories();
                List<Supplier> suppliers = getSuppliers();
                
                model.addAttribute("categories", categories);
                model.addAttribute("suppliers", suppliers);
                model.addAttribute("productStatuses", ProductStatus.values());
                
                return "product/form";
            }
            
            // 상품 코드 중복 확인 (자기 자신 제외)
            if (productService.isCodeDuplicate(updateDto.getCode(), productSeq)) {
                bindingResult.rejectValue("code", "duplicate", "이미 사용 중인 상품 코드입니다.");
                
                List<Category> categories = getCategories();
                List<Supplier> suppliers = getSuppliers();
                
                model.addAttribute("categories", categories);
                model.addAttribute("suppliers", suppliers);
                model.addAttribute("productStatuses", ProductStatus.values());
                
                return "product/form";
            }
            
            updateDto.setProductSeq(productSeq);
            ProductDto updatedProduct = productService.update(updateDto);
            log.info("상품 수정 성공: productSeq={}", updatedProduct.getProductSeq());
            
            redirectAttributes.addFlashAttribute("success", "상품이 성공적으로 수정되었습니다.");
            return "redirect:/products/" + updatedProduct.getProductSeq();
            
        } catch (Exception e) {
            log.error("상품 수정 오류: productSeq={}, error={}", productSeq, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "상품 수정 중 오류가 발생했습니다.");
            return "redirect:/products";
        }
    }

    /**
     * 상품 삭제
     */
    @PostMapping("/{productSeq}/delete")
    public String delete(@PathVariable Long productSeq, RedirectAttributes redirectAttributes) {
        log.info("상품 삭제 요청: productSeq={}", productSeq);
        
        try {
            productService.delete(productSeq);
            log.info("상품 삭제 성공: productSeq={}", productSeq);
            
            redirectAttributes.addFlashAttribute("success", "상품이 성공적으로 삭제되었습니다.");
            return "redirect:/products";
            
        } catch (Exception e) {
            log.error("상품 삭제 오류: productSeq={}, error={}", productSeq, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "상품 삭제 중 오류가 발생했습니다.");
            return "redirect:/products";
        }
    }
}
