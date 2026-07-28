package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.User;

public interface UserRepository extends
                JpaRepository<User, UUID>,
                JpaSpecificationExecutor<User> {

        Page<User> findByDeletedAtIsNull(Pageable pageable);

        Optional<User> findByIdAndDeletedAtIsNull(UUID id);

        Optional<User> findByEmailAndDeletedAtIsNull(
                        String email);

        boolean existsByEmailAndDeletedAtIsNull(
                        String email);

        boolean existsByEmail(
                        String email);

        boolean existsByEmailAndIdNot(
                        String email,
                        UUID id);

        boolean existsByIdAndDeletedAtIsNull(UUID id);
    Optional<User> findByGoogleIdAndDeletedAtIsNull(String googleId);
        @EntityGraph(attributePaths = {"role"})
        Page<User> findAll(Specification<User> spec, Pageable pageable);
}
