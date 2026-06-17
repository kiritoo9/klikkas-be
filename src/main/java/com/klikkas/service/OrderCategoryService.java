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

import com.klikkas.dto.order_categories.OrderCategoryListResponse;
import com.klikkas.dto.order_categories.OrderCategoryRequest;
import com.klikkas.dto.order_categories.OrderCategoryResponse;
import com.klikkas.entity.OrderCategory;
import com.klikkas.entity.Tenant;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.OrderCategoryRepository;
import com.klikkas.repository.TenantRepository;
import com.klikkas.security.TenantContext;
import com.klikkas.specification.OrderCategorySpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCategoryService {

        private final OrderCategoryRepository categoryRepository;
        private final TenantRepository tenantRepository;

        public OrderCategoryListResponse getCategories(
                        Integer page,
                        Integer limit,
                        String order,
                        String dir,
                        String keywords,
                        String categoryType) {

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

                Specification<OrderCategory> spec = Specification
                                .where(OrderCategorySpecification.notDeleted())
                                .and(OrderCategorySpecification.categoryType(categoryType))
                                .and(OrderCategorySpecification.byTenant(tenantID))
                                .and(OrderCategorySpecification.keyword(keywords));

                Page<OrderCategory> result = categoryRepository.findAll(spec, pageable);

                List<OrderCategoryResponse> data = result
                                .getContent()
                                .stream()
                                .map(c -> new OrderCategoryResponse(
                                                c.getId(),
                                                c.getName(),
                                                c.getDescription(),
                                                c.getCategoryType(),
                                                c.getIsActive(),
                                                c.getCreatedAt()))
                                .toList();

                return new OrderCategoryListResponse(
                                data,
                                page,
                                result.getTotalPages());
        }

        public OrderCategoryResponse getCategory(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                OrderCategory category = categoryRepository.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                return new OrderCategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getDescription(),
                                category.getCategoryType(),
                                category.getIsActive(),
                                category.getCreatedAt());
        }

        public OrderCategoryResponse createCategory(OrderCategoryRequest req) {
                if (categoryRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(req.name())) {
                        throw new BadRequestException("Name already taken");
                }

                UUID tenantID = TenantContext.getTenantId();
                Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(tenantID)
                                .orElseThrow(() -> new BadRequestException("Tenant is not valid"));

                OrderCategory category = new OrderCategory();
                category.setName(req.name());
                category.setTenant(tenant);
                category.setDescription(req.description());
                category.setCategoryType(req.category_type());
                category.setIsActive(req.is_active());

                OrderCategory saved = categoryRepository.save(category);
                return new OrderCategoryResponse(
                                saved.getId(),
                                saved.getName(),
                                saved.getDescription(),
                                saved.getCategoryType(),
                                saved.getIsActive(),
                                saved.getCreatedAt());
        }

        public void updateCategory(UUID id, OrderCategoryRequest req) {
                UUID tenantID = TenantContext.getTenantId();
                OrderCategory category = categoryRepository.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                if (categoryRepository.existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(req.name(), id)) {
                        throw new BadRequestException("Name already taken");
                }

                category.setName(req.name());
                category.setCategoryType(req.category_type());
                category.setDescription(req.description());
                category.setIsActive(req.is_active());
                categoryRepository.save(category);
        }

        public void deleteCategory(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                OrderCategory category = categoryRepository.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                category.setDeletedAt(LocalDateTime.now());
                categoryRepository.save(category);
        }

}
