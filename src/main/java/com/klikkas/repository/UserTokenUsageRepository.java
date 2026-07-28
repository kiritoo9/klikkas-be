package com.klikkas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.UserTokenUsages;

public interface UserTokenUsageRepository extends 
    JpaRepository<UserTokenUsages, UUID>,
    JpaSpecificationExecutor<UserTokenUsages> {
    
}
