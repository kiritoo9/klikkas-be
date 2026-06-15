package com.klikkas.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.klikkas.entity.Role;

public interface RoleRepository extends
        JpaRepository<Role, UUID>,
        JpaSpecificationExecutor<Role>{

    Optional<Role> findByIdAndDeletedAtIsNull(UUID roleId);
}