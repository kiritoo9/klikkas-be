package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.Account;

public interface AccountRepository extends
        JpaRepository<Account, UUID>,
        JpaSpecificationExecutor<Account> {

    Optional<Account> findByCodeAndDeletedAtIsNull(String debitAccountCode);

    Optional<Account> findByIdAndDeletedAtIsNull(UUID debit_account_id);

}
