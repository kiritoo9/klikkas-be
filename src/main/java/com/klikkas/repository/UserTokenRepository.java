package com.klikkas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.UserToken;

public interface UserTokenRepository extends
    JpaRepository<UserToken, UUID> {

    UserToken findByUserId(UUID userID);
    
}
