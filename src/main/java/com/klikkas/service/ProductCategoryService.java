package com.klikkas.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.product_categories.ProductCategoryRequest;
import com.klikkas.dto.product_categories.ProductCategoryListResponse;
import com.klikkas.dto.product_categories.ProductCategoryResponse;
import com.klikkas.entity.ProductCategory;
import com.klikkas.entity.Tenant;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.ProductCategoryRepository;
import com.klikkas.repository.TenantRepository;
import com.klikkas.security.TenantContext;
import com.klikkas.specification.ProductCategorySpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

        public final ProductCategoryRepository categoryRepo;
        public final TenantRepository tenantRepository;

        public ProductCategoryListResponse getCategories(
                        Integer page,
                        Integer limit,
                        String order,
                        String dir,
                        String keywords) {
                UUID tenantID = TenantContext.getTenantId();
                Pageable pageable;

                if (order != "" && !order.isBlank()) {
                        Sort sort = dir.equalsIgnoreCase("desc")
                                        ? Sort.by(order).descending()
                                        : Sort.by(order).ascending();

                        pageable = PageRequest.of(page - 1, limit, sort);
                } else {
                        pageable = PageRequest.of(page - 1, limit);
                }

                // perform query
                Specification<ProductCategory> spec = Specification
                                .where(ProductCategorySpecification.notDeleted())
                                .and(ProductCategorySpecification.byTenant(tenantID))
                                .and(ProductCategorySpecification.keyword(keywords));

                Page<ProductCategory> result = categoryRepo.findAll(spec, pageable);

                List<ProductCategoryResponse> data = result
                                .getContent()
                                .stream()
                                .map(c -> new ProductCategoryResponse(
                                                c.getId(),
                                                c.getName(),
                                                c.getDescription(),
                                                c.getCreatedAt()))
                                .toList();

                return new ProductCategoryListResponse(
                                data,
                                page,
                                result.getTotalPages());
        }

        public ProductCategoryResponse getCategory(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                ProductCategory category = categoryRepo.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException(
                                                "Data not found",
                                                "DATA_NOT_FOUND"));

                return new ProductCategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getDescription(),
                                category.getCreatedAt());
        }

        public ProductCategoryResponse createCategory(ProductCategoryRequest req) {
                if (categoryRepo.existsByNameIgnoreCase(req.name())) {
                        throw new BadRequestException("Name already taken");
                }

                UUID tenantID = TenantContext.getTenantId();
                Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(tenantID)
                                .orElseThrow(() -> new BadRequestException("Invalid tenant"));

                ProductCategory category = new ProductCategory();
                category.setTenant(tenant);
                category.setName(req.name());
                category.setDescription(req.description());
                category.setIsActive(req.is_active());
                ProductCategory saved = categoryRepo.save(category);

                return new ProductCategoryResponse(
                                saved.getId(),
                                saved.getName(),
                                saved.getDescription(),
                                saved.getCreatedAt());
        }

        public void updateCategory(UUID id, ProductCategoryRequest req) {
                UUID tenantID = TenantContext.getTenantId();
                ProductCategory category = categoryRepo.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException(
                                                "Data not found",
                                                "DATA_NOT_FOUND"));

                if (categoryRepo.existsByNameIgnoreCaseAndIdNot(req.name(), id)) {
                        throw new BadRequestException("Name already taken");
                }

                category.setName(req.name());
                category.setDescription(req.description());
                category.setIsActive(req.is_active());
                categoryRepo.save(category);
        }

        public void deleteCategory(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                ProductCategory category = categoryRepo.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException(
                                                "Data not found",
                                                "DATA_NOT_FOUND"));

                category.setDeletedAt(LocalDateTime.now());
                categoryRepo.save(category);
        }

}
