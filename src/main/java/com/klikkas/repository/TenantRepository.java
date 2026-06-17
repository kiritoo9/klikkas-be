package com.klikkas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.Tenant;

public interface TenantRepository extends
        JpaRepository<Tenant, UUID> {

}