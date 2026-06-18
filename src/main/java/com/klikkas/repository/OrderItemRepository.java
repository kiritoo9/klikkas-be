package com.klikkas.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.OrderItem;

public interface OrderItemRepository extends
                JpaRepository<OrderItem, UUID>,
                JpaSpecificationExecutor<OrderItem> {

        List<OrderItem> findByOrderIdAndDeletedAtIsNull(UUID id);

}
