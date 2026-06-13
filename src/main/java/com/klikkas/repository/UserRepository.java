package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.User;

public interface UserRepository 
    extends JpaRepository<User, UUID> {

    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    User findByEmailAndDeletedAtIsNull(
            String email);

    boolean existsByEmailAndDeletedAtIsNull(
            String email);

    boolean existsByEmail(
            String email);
}
