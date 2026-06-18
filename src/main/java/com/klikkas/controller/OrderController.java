package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.orders.OrderDetailResponse;
import com.klikkas.dto.orders.OrderListResponse;
import com.klikkas.dto.orders.OrderRequest;
import com.klikkas.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public OrderListResponse getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = true, defaultValue = "kas_masuk") String orderType,
            @RequestParam(defaultValue = "createdAt", required = false) String order,
            @RequestParam(defaultValue = "desc", required = false) String dir,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) UUID categoryId) {
        return orderService.getOrders(page, limit, orderType, order, dir, keywords, categoryId);
    }

    @GetMapping("/orders/{id}")
    public OrderDetailResponse getOrder(
            @PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/orders")
    public OrderDetailResponse createOrder(
            @Valid @RequestBody OrderRequest req) {
        return orderService.createOrder(req);
    }

    @PutMapping("/orders/{id}")
    public ResponseEntity<Void> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody OrderRequest req) {
        orderService.updateOrder(id, req);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable UUID id) {
        orderService.deleteOrder(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
