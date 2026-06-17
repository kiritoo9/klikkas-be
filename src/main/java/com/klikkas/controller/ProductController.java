package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.products.ProductListResponse;
import com.klikkas.dto.products.ProductRequest;
import com.klikkas.dto.products.ProductResponse;
import com.klikkas.service.ProductService;

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
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ProductListResponse getProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "createdAt", required = false) String order,
            @RequestParam(defaultValue = "desc", required = false) String dir,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) UUID category_id) {
        return productService.getProducts(
                page,
                limit,
                order,
                dir,
                keywords,
                category_id);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(
            @PathVariable UUID id) {
        return productService.getProduct(id);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest req) {
        return productService.createProduct(req);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest req) {
        productService.updateProduct(id, req);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID id) {
        productService.deleteProduct(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
