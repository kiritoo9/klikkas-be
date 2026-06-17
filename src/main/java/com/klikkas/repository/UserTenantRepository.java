package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.UserTenant;

public interface UserTenantRepository extends JpaRepository<UserTenant, UUID> {

    Optional<UserTenant> findByUserId(UUID id);

}
