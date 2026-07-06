package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.order_categories.OrderCategoryListResponse;
import com.klikkas.dto.order_categories.OrderCategoryRequest;
import com.klikkas.dto.order_categories.OrderCategoryResponse;
import com.klikkas.service.OrderCategoryService;

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
public class OrderCategoryController {

    private final OrderCategoryService categoryService;

    @GetMapping("/order_categories")
    public OrderCategoryListResponse getCategories(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "createdAt", required = false) String order,
            @RequestParam(defaultValue = "desc", required = false) String dir,
            @RequestParam(defaultValue = "", required = false) String categoryType,
            @RequestParam(required = false) String keywords) {
        return categoryService.getCategories(page, limit, order, dir, keywords, categoryType);
    }

    @GetMapping("/order_categories/{id}")
    public OrderCategoryResponse getCategory(
            @PathVariable UUID id) {
        return categoryService.getCategory(id);
    }

    @PostMapping("/order_categories")
    public OrderCategoryResponse createCategory(
            @Valid @RequestBody OrderCategoryRequest req) {
        return categoryService.createCategory(req);
    }

    @PutMapping("/order_categories/{id}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody OrderCategoryRequest req) {
        categoryService.updateCategory(id, req);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/order_categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
