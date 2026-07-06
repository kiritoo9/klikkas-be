package com.klikkas.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.accounts.AccountListResponse;
import com.klikkas.dto.accounts.AccountResponse;
import com.klikkas.entity.Account;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.AccountRepository;
import com.klikkas.specification.AccountSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

        public final AccountRepository accountRepository;

        public AccountListResponse getAccounts(
                        Integer page,
                        Integer limit,
                        String order,
                        String dir,
                        String keywords) {
                Pageable pageable;
                if (order != null && !order.isBlank()) {
                        Sort sort = dir.equalsIgnoreCase("desc")
                                        ? Sort.by(order).descending()
                                        : Sort.by(order).ascending();

                        pageable = PageRequest.of(page - 1, limit, sort);
                } else {
                        pageable = PageRequest.of(page - 1, limit);
                }

                // perform query
                Specification<Account> spec = Specification
                                .where(AccountSpecification.notDeleted())
                                .and(AccountSpecification.keyword(keywords));

                Page<Account> result = accountRepository.findAll(
                                spec,
                                pageable);

                List<AccountResponse> accounts = result
                                .getContent()
                                .stream().map(account -> new AccountResponse(
                                                account.getId(),
                                                account.getCode(),
                                                account.getName(),
                                                account.getType(),
                                                account.getParent_id(),
                                                account.getIs_active(),
                                                account.getCreatedAt()))
                                .toList();

                return new AccountListResponse(
                                accounts,
                                page,
                                result.getTotalPages());
        }

        public AccountResponse getAccount(UUID id) {
                Account account = accountRepository.findByIdAndDeletedAtIsNull(id)
                                .orElseThrow(() -> new NotFoundException("DATA_NOT_FOUND", "Data not found"));

                return new AccountResponse(
                                account.getId(),
                                account.getCode(),
                                account.getName(),
                                account.getType(),
                                account.getParent_id(),
                                account.getIs_active(),
                                account.getCreatedAt());
        }

}
