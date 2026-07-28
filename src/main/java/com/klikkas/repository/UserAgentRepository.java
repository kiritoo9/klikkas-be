package com.klikkas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.UserAgent;

public interface UserAgentRepository extends
    JpaRepository<UserAgent, UUID> {
    
}
