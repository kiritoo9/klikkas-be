package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.UserPackage;

public interface UserPackageRepository extends 
    JpaRepository<UserPackage, UUID> {

    Optional<UserPackage> findByUserIdAndDeletedAtIsNull(UUID id);
    
}
