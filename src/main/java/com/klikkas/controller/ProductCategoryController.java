package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.product_categories.ProductCategoryRequest;
import com.klikkas.dto.product_categories.ProductCategoryListResponse;
import com.klikkas.dto.product_categories.ProductCategoryResponse;
import com.klikkas.service.ProductCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping("/product_categories")
    public ProductCategoryListResponse getCategories(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "createdAt", required = false) String order,
            @RequestParam(defaultValue = "desc", required = false) String dir,
            @RequestParam(required = false) String keywords) {
        return categoryService.getCategories(page, limit, order, dir, keywords);
    }

    @GetMapping("/product_categories/{id}")
    public ProductCategoryResponse getCategory(
            @PathVariable UUID id) {
        return categoryService.getCategory(id);
    }

    @PostMapping("/product_categories")
    public ProductCategoryResponse createCategory(
            @Valid @RequestBody ProductCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/product_categories/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody ProductCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/product_categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
