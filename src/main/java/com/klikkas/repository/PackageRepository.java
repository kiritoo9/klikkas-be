package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.Packages;

public interface PackageRepository extends
    JpaRepository<Packages, UUID> {

    Optional<Packages> findByCodeAndDeletedAtIsNull(String string);
}