package com.klikkas.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.orders.OrderDetailResponse;
import com.klikkas.dto.orders.OrderItemRequest;
import com.klikkas.dto.orders.OrderItemResponse;
import com.klikkas.dto.orders.OrderListResponse;
import com.klikkas.dto.orders.OrderRequest;
import com.klikkas.dto.orders.OrderResponse;
import com.klikkas.entity.Account;
import com.klikkas.entity.Journal;
import com.klikkas.entity.JournalDetail;
import com.klikkas.entity.Order;
import com.klikkas.entity.OrderCategory;
import com.klikkas.entity.OrderItem;
import com.klikkas.entity.Product;
import com.klikkas.entity.Tenant;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.AccountRepository;
import com.klikkas.repository.JournalDetailRepository;
import com.klikkas.repository.JournalRepository;
import com.klikkas.repository.OrderCategoryRepository;
import com.klikkas.repository.OrderItemRepository;
import com.klikkas.repository.OrderRepository;
import com.klikkas.repository.ProductRepository;
import com.klikkas.repository.TenantRepository;
import com.klikkas.security.TenantContext;
import com.klikkas.specification.OrderSpecification;
import com.klikkas.util.DateTimeParser;
import com.klikkas.util.RandomString;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

        private final OrderRepository orderRepo;
        private final OrderItemRepository orderItemRepo;
        private final OrderCategoryRepository categoryRepo;
        private final TenantRepository tenantRepo;
        private final ProductRepository productRepo;
        private final JournalRepository journalRepo;
        private final JournalDetailRepository journalDetailRepo;
        private final AccountRepository accountRepository;

        private OrderDetailResponse translateDetail(
                        Order order,
                        List<OrderItem> orderItem) {
                List<OrderItemResponse> items = orderItem
                                .stream()
                                .map(i -> new OrderItemResponse(
                                                i.getId(),
                                                i.getProduct() != null ? i.getProduct().getId() : null,
                                                i.getItemName(),
                                                i.getItemDesc(),
                                                i.getPrice(),
                                                i.getQty(),
                                                i.getRemark(),
                                                i.getCreatedAt()))
                                .toList();

                return new OrderDetailResponse(
                                new OrderResponse(
                                                order.getId(),
                                                order.getCategory().getId(),
                                                order.getOrderType(),

                                                order.getNoOrder(),
                                                order.getOrderDate(),

                                                order.getTotalQty(),
                                                order.getTotalPrice(),
                                                order.getTaxAmount(),
                                                order.getDiscountAmount(),
                                                order.getGrandTotal(),
                                                order.getStatus(),
                                                order.getRemark(),

                                                order.getCreatedAt()),
                                items);
        }

        public OrderListResponse getOrders(
                        Integer page,
                        Integer limit,
                        String orderType,
                        String order,
                        String dir,
                        String keywords,
                        UUID categoryID) {
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

                Specification<Order> spec = Specification
                                .where(OrderSpecification.notDeleted())
                                .and(OrderSpecification.byTenant(tenantID))
                                .and(OrderSpecification.byType(orderType))
                                .and(OrderSpecification.byCategory(categoryID));

                Page<Order> result = orderRepo.findAll(spec, pageable);

                List<OrderResponse> data = result
                                .getContent()
                                .stream()
                                .map(o -> new OrderResponse(
                                                o.getId(),
                                                o.getCategory().getId(),
                                                o.getOrderType(),

                                                o.getNoOrder(),
                                                o.getOrderDate(),

                                                o.getTotalQty(),
                                                o.getTotalPrice(),
                                                o.getTaxAmount(),
                                                o.getDiscountAmount(),
                                                o.getGrandTotal(),
                                                o.getStatus(),
                                                o.getRemark(),

                                                o.getCreatedAt()))
                                .toList();

                return new OrderListResponse(
                                data,
                                page,
                                result.getTotalPages());
        }

        public OrderDetailResponse getOrder(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                Order order = orderRepo.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                List<OrderItem> resultItem = orderItemRepo.findByOrderIdAndDeletedAtIsNull(id);
                return translateDetail(order, resultItem);
        }

        @Transactional
        public OrderDetailResponse createOrder(OrderRequest req) {
                UUID tenantID = TenantContext.getTenantId();

                Tenant tenant = tenantRepo.findByIdAndDeletedAtIsNull(tenantID)
                                .orElseThrow(() -> new BadRequestException("Invalid tenant"));

                OrderCategory category = categoryRepo.findByIdAndDeletedAtIsNull(req.category_id())
                                .orElseThrow(() -> new BadRequestException("Invalid category"));

                // Preparing data
                Integer totalQty = 0;
                Integer totalPrice = 0;
                List<OrderItem> orderItems = new ArrayList<>();
                for (OrderItemRequest item : req.items()) {
                        totalQty += item.qty();
                        totalPrice += item.qty() * item.price();

                        OrderItem orderItem = new OrderItem();

                        if (item.product_id() != null) {
                                Product product = productRepo.findByIdAndDeletedAtIsNull(item.product_id())
                                                .orElseThrow(() -> new BadRequestException("Invalid product"));
                                orderItem.setProduct(product);
                        }
                        orderItem.setItemName(item.item_name());
                        orderItem.setItemDesc(item.item_desc());
                        orderItem.setQty(item.qty());
                        orderItem.setPrice(item.price());
                        orderItem.setRemark(item.remark());

                        orderItems.add(orderItem);
                }

                // Perform to query
                Order order = new Order();
                order.setCategory(category);
                order.setTenant(tenant);

                order.setNoOrder(RandomString.randomString(20));
                order.setOrderDate(DateTimeParser.parseDateOrNow(req.order_date()));
                order.setStatus(req.status());
                order.setRemark(req.remark());

                Integer grandTotal = totalPrice + req.tax_amount() - req.discount_amount();
                order.setTotalQty(totalQty);
                order.setTotalPrice(totalPrice);
                order.setTaxAmount(req.tax_amount());
                order.setDiscountAmount(req.discount_amount());
                order.setGrandTotal(grandTotal);

                order = orderRepo.save(order);
                for (OrderItem item : orderItems) {
                        item.setOrder(order);
                }
                orderItemRepo.saveAll(orderItems);

                // insert journal ONLY when category.debit_account_id and
                // category.credit_account_id is not null
                if (category.getDebitAccount() != null && category.getCreditAccount() != null) {
                        Account debitAccount = accountRepository
                                        .findByIdAndDeletedAtIsNull(category.getDebitAccount().getId())
                                        .orElseThrow(() -> new BadRequestException(
                                                        "Invalid debit account journal for this category"));

                        Account creditAccount = accountRepository
                                        .findByIdAndDeletedAtIsNull(category.getCreditAccount().getId())
                                        .orElseThrow(() -> new BadRequestException(
                                                        "Invalid credit account journal for this category"));

                        Journal journal = new Journal();
                        journal.setJournalDate(LocalDateTime.now());
                        journal.setReferenceType(req.order_type());
                        journal.setReferenceId(order.getId());
                        journal.setDescription("Transaction from order " + req.order_type());
                        journal = journalRepo.save(journal);

                        JournalDetail debitJournal = new JournalDetail();
                        debitJournal.setJournal(journal);
                        debitJournal.setAccount(debitAccount);
                        debitJournal.setDebit(order.getGrandTotal());
                        debitJournal.setCredit(0);
                        debitJournal.setRemark("auto_journal");
                        journalDetailRepo.save(debitJournal);

                        JournalDetail creditJournal = new JournalDetail();
                        creditJournal.setJournal(journal);
                        creditJournal.setAccount(creditAccount);
                        creditJournal.setDebit(0);
                        creditJournal.setCredit(order.getGrandTotal());
                        creditJournal.setRemark("auto_journal");
                        journalDetailRepo.save(creditJournal);
                }

                // response
                return translateDetail(order, orderItems);
        }

        @Transactional
        public void updateOrder(UUID id, OrderRequest req) {
                UUID tenantID = TenantContext.getTenantId();
                Order order = orderRepo.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                if (req.category_id() != null) {
                        OrderCategory category = categoryRepo.findByIdAndDeletedAtIsNull(req.category_id())
                                        .orElseThrow(() -> new NotFoundException("Category not found",
                                                        "DATA_NOT_FOUND"));
                        order.setCategory(category);
                }

                if (req.order_date() != null) {
                        order.setOrderDate(DateTimeParser.parseDateOrNow(req.order_date()));
                }
                if (req.tax_amount() != null) {
                        order.setTaxAmount(req.tax_amount());
                }
                if (req.discount_amount() != null) {
                        order.setDiscountAmount(req.discount_amount());
                }
                if (req.status() != null) {
                        order.setStatus(req.status());
                }
                if (req.remark() != null) {
                        order.setRemark(req.remark());
                }

                if (req.items() != null && !req.items().isEmpty()) {
                        List<OrderItem> existingItems = orderItemRepo.findByOrderIdAndDeletedAtIsNull(id);
                        for (OrderItem item : existingItems) {
                                item.setDeletedAt(java.time.LocalDateTime.now());
                        }
                        orderItemRepo.saveAll(existingItems);

                        Integer totalQty = 0;
                        Integer totalPrice = 0;
                        List<OrderItem> newItems = new ArrayList<>();
                        for (OrderItemRequest itemReq : req.items()) {
                                totalQty += itemReq.qty();
                                totalPrice += itemReq.qty() * itemReq.price();

                                OrderItem newItem = new OrderItem();
                                newItem.setOrder(order);

                                if (itemReq.product_id() != null) {
                                        Product product = productRepo.findByIdAndDeletedAtIsNull(itemReq.product_id())
                                                        .orElseThrow(() -> new BadRequestException("Invalid product"));
                                        newItem.setProduct(product);
                                }
                                newItem.setItemName(itemReq.item_name());
                                newItem.setItemDesc(itemReq.item_desc());
                                newItem.setQty(itemReq.qty());
                                newItem.setPrice(itemReq.price());
                                newItem.setRemark(itemReq.remark());

                                newItems.add(newItem);
                        }
                        orderItemRepo.saveAll(newItems);

                        // Update totals
                        order.setTotalQty(totalQty);
                        order.setTotalPrice(totalPrice);
                }

                // Recalculate grand total
                Integer grandTotal = order.getTotalPrice() + order.getTaxAmount() - order.getDiscountAmount();
                order.setGrandTotal(grandTotal);

                orderRepo.save(order);

                // re-write journal: delete by reference_id then re-insert
                java.util.List<Journal> existingJournals = journalRepo.findAll()
                                .stream()
                                .filter(j -> j.getReferenceId() != null
                                                && j.getReferenceId().equals(order.getId())
                                                && j.getReferenceType() != null
                                                && j.getReferenceType().equals(order.getOrderType())
                                                && j.getDeletedAt() == null)
                                .toList();
                for (Journal j : existingJournals) {
                        j.setDeletedAt(java.time.LocalDateTime.now());
                }
                journalRepo.saveAll(existingJournals);

                // re-insert journal if category has debit/credit accounts
                OrderCategory category = order.getCategory();
                if (category.getDebitAccount() != null && category.getCreditAccount() != null) {
                        Account debitAccount = accountRepository
                                        .findByIdAndDeletedAtIsNull(category.getDebitAccount().getId())
                                        .orElseThrow(() -> new BadRequestException(
                                                        "Invalid debit account journal for this category"));

                        Account creditAccount = accountRepository
                                        .findByIdAndDeletedAtIsNull(category.getCreditAccount().getId())
                                        .orElseThrow(() -> new BadRequestException(
                                                        "Invalid credit account journal for this category"));

                        Journal journal = new Journal();
                        journal.setJournalDate(LocalDateTime.now());
                        journal.setReferenceType(order.getOrderType());
                        journal.setReferenceId(order.getId());
                        journal.setDescription("Transaction from order " + order.getOrderType());
                        journal = journalRepo.save(journal);

                        JournalDetail debitJournal = new JournalDetail();
                        debitJournal.setJournal(journal);
                        debitJournal.setAccount(debitAccount);
                        debitJournal.setDebit(order.getGrandTotal());
                        debitJournal.setCredit(0);
                        debitJournal.setRemark("auto_journal");
                        journalDetailRepo.save(debitJournal);

                        JournalDetail creditJournal = new JournalDetail();
                        creditJournal.setJournal(journal);
                        creditJournal.setAccount(creditAccount);
                        creditJournal.setDebit(0);
                        creditJournal.setCredit(order.getGrandTotal());
                        creditJournal.setRemark("auto_journal");
                        journalDetailRepo.save(creditJournal);
                }
        }

        @Transactional
        public void deleteOrder(UUID id) {
                UUID tenantID = TenantContext.getTenantId();
                Order order = orderRepo.findByIdAndDeletedAtIsNullAndTenantId(id, tenantID)
                                .orElseThrow(() -> new NotFoundException("Data not found", "DATA_NOT_FOUND"));

                // soft delete order items
                List<OrderItem> existingItems = orderItemRepo.findByOrderIdAndDeletedAtIsNull(id);
                for (OrderItem item : existingItems) {
                        item.setDeletedAt(java.time.LocalDateTime.now());
                }
                orderItemRepo.saveAll(existingItems);

                List<Journal> existingJournals = journalRepo.findAll()
                                .stream()
                                .filter(j -> j.getReferenceId() != null
                                                && j.getReferenceId().equals(order.getId())
                                                && j.getReferenceType() != null
                                                && j.getReferenceType().equals(order.getOrderType())
                                                && j.getDeletedAt() == null)
                                .toList();
                for (Journal j : existingJournals) {
                        java.util.List<JournalDetail> details = journalDetailRepo.findAll()
                                        .stream()
                                        .filter(d -> d.getJournal() != null
                                                        && d.getJournal().getId().equals(j.getId())
                                                        && d.getDeletedAt() == null)
                                        .toList();
                        for (JournalDetail detail : details) {
                                detail.setDeletedAt(LocalDateTime.now());
                        }
                        journalDetailRepo.saveAll(details);

                        // soft delete journal
                        j.setDeletedAt(LocalDateTime.now());
                }
                journalRepo.saveAll(existingJournals);

                // soft delete order
                order.setDeletedAt(LocalDateTime.now());
                orderRepo.save(order);
        }

}
