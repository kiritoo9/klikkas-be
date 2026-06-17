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

import com.klikkas.dto.products.ProductListResponse;
import com.klikkas.dto.products.ProductRequest;
import com.klikkas.dto.products.ProductResponse;
import com.klikkas.entity.Product;
import com.klikkas.entity.ProductCategory;
import com.klikkas.entity.Tenant;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.ProductCategoryRepository;
import com.klikkas.repository.ProductRepository;
import com.klikkas.repository.TenantRepository;
import com.klikkas.security.TenantContext;
import com.klikkas.specification.ProductSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

        private final ProductRepository productRepository;
        private final ProductCategoryRepository categoryRepository;
        private final TenantRepository tenantRepository;

        public ProductListResponse getProducts(
                        Integer page,
                        Integer limit,
                        String order,
                        String dir,
                        String keywords,
                        UUID categoryId) {
                UUID tenantID = TenantContext.getTenantId();
                Pageable pageable;

                if (order != "" && !order.isBlank()) {
                        Sort sort = dir.equalsIgnoreCase("desc")
                                        ? Sort.by(order).descending()
                                        : Sort.by(order).ascending();

                        // pageable = Pageable(page - 1, limit, sort);
                        pageable = PageRequest.of(page - 1, limit, sort);
                } else {
                        pageable = PageRequest.of(page - 1, limit);
                }

                Specification<Product> spec = Specification
                                .where(ProductSpecification.notDeleted())
                                .and(ProductSpecification.byTenant(tenantID))
                                .and(ProductSpecification.category(categoryId))
                                .and(ProductSpecification.keyword(keywords));

                Page<Product> result = productRepository.findAll(spec, pageable);
                List<ProductResponse> data = result
                                .getContent()
                                .stream()
                                .map(p -> new ProductResponse(
                                                p.getId(),
                                                p.getCategory().getId(),
                                                p.getCategory().getName(),
                                                p.getSku(),
                                                p.getName(),
                                                p.getDescription(),
                                                p.getBuyPrice(),
                                                p.getSellPrice(),
                                                p.getStock(),
                                                p.getCreatedAt()))
                                .toList();

                return new ProductListResponse(
                                data,
                                page,
                                result.getTotalPages());
        }

        public ProductResponse getProduct(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                Product product = productRepository.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                return new ProductResponse(
                                product.getId(),
                                product.getCategory().getId(),
                                product.getCategory().getName(),
                                product.getSku(),
                                product.getName(),
                                product.getDescription(),
                                product.getBuyPrice(),
                                product.getSellPrice(),
                                product.getStock(),
                                product.getCreatedAt());
        }

        public ProductResponse createProduct(ProductRequest req) {
                if (productRepository.existsBySkuIgnoreCaseAndDeletedAtIsNull(req.sku())) {
                        throw new BadRequestException("SKU already taken");
                }

                ProductCategory category = categoryRepository.findByIdAndDeletedAtIsNull(req.category_id())
                                .orElseThrow(() -> new NotFoundException("Category not found", "DATA_NOT_FOUND"));

                UUID tenantID = TenantContext.getTenantId();
                Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(tenantID)
                                .orElseThrow(() -> new BadRequestException("Invalid tenant"));

                Product product = new Product();
                product.setCategory(category);
                product.setTenant(tenant);
                product.setSku(req.sku());
                product.setName(req.name());
                product.setDescription(req.description());
                product.setBuyPrice(req.buy_price());
                product.setSellPrice(req.sell_price());
                product.setStock(req.stock());
                product.setIsActive(req.is_active());

                Product saved = productRepository.save(product);
                return new ProductResponse(
                                saved.getId(),
                                saved.getCategory().getId(),
                                saved.getCategory().getName(),
                                saved.getSku(),
                                saved.getName(),
                                saved.getDescription(),
                                saved.getBuyPrice(),
                                saved.getSellPrice(),
                                saved.getStock(),
                                saved.getCreatedAt());
        }

        public void updateProduct(UUID id, ProductRequest req) {
                UUID tenantID = TenantContext.getTenantId();
                Product product = productRepository.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                if (productRepository.existsBySkuIgnoreCaseAndDeletedAtIsNullAndIdNot(req.sku(), id)) {
                        throw new BadRequestException("SKU already taken");
                }

                ProductCategory category = categoryRepository.findByIdAndDeletedAtIsNull(req.category_id())
                                .orElseThrow(() -> new NotFoundException("Category not found", "DATA_NOT_FOUND"));

                product.setCategory(category);
                product.setSku(req.sku());
                product.setName(req.name());
                product.setDescription(req.description());
                product.setBuyPrice(req.buy_price());
                product.setSellPrice(req.sell_price());
                product.setStock(req.stock());
                product.setIsActive(req.is_active());
                productRepository.save(product);
        }

        public void deleteProduct(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                Product product = productRepository.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                product.setDeletedAt(LocalDateTime.now());
                productRepository.save(product);
        }

}
