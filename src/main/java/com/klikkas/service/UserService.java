package com.klikkas.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.entity.Role;
import com.klikkas.entity.User;
import com.klikkas.dto.users.CreateUserRequest;
import com.klikkas.dto.users.UpdateUserRequest;
import com.klikkas.dto.users.UserListResponse;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.repository.RoleRepository;
import com.klikkas.repository.UserRepository;
import com.klikkas.specification.UserSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    public final UserRepository userRepository;
    public final RoleRepository roleRepository;

    public UserListResponse getUsers(
            Integer page,
            Integer limit,
            String order,
            String dir,
            String keywords) {
        // preparing pageable parameters
        Pageable pageable;
        if (order != null && !order.isBlank()) {
            Sort sort = dir.equalsIgnoreCase("desc")
                    ? Sort.by(order).descending()
                    : Sort.by(order).ascending();

            pageable = PageRequest.of(page - 1, limit, sort);
        } else {
            pageable = PageRequest.of(page - 1, limit);
        }

        // perform query
        Specification<User> spec = Specification
                .where(UserSpecification.notDeleted())
                .and(UserSpecification.isNotRoot())
                .and(UserSpecification.keyword(keywords));

        Page<User> result = userRepository.findAll(
                spec,
                pageable);

        List<UserResponse> users = result.getContent().stream().map(user -> new UserResponse(
                user.getId(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getEmail(),
                user.getFullname(),
                user.getPhone(),
                user.getAddress(),
                user.getRemark(),
                user.getCreatedAt())).toList();

        return new UserListResponse(
                users,
                page,
                result.getTotalPages());
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one digit");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new BadRequestException("Password must contain at least one special character");
        }
    }

    public UserResponse getUser(UUID id) {
        Specification<User> spec = Specification
                .where(UserSpecification.notDeleted())
                .and(UserSpecification.isNotRoot())
                .and(UserSpecification.byId(id));

        User user = userRepository
                .findOne(spec)
                .orElseThrow(() -> new NotFoundException(
                        "Data not found",
                        "DATA_NOT_FOUND"));

        return new UserResponse(
                user.getId(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getEmail(),
                user.getFullname(),
                user.getPhone(),
                user.getAddress(),
                user.getRemark(),
                user.getCreatedAt());
    }

    public UserResponse createUser(CreateUserRequest request) {
        // validate input
        validatePassword(request.password());

        // validate query
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByIdAndDeletedAtIsNull(request.role_id())
                .orElseThrow(() -> new BadRequestException("Invalid role id"));

        User user = new User();
        user.setEmail(request.email());
        user.setRole(role);
        user.setFullname(request.fullname());

        // validate and encrypt password
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRemark(request.remark());

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getRole().getId(),
                saved.getRole().getName(),
                saved.getEmail(),
                saved.getFullname(),
                saved.getPhone(),
                saved.getAddress(),
                saved.getRemark(),
                saved.getCreatedAt());
    }

    public void updateUser(UUID id, UpdateUserRequest request) {
        // validate input
        if (request.password() != null && !request.password().isBlank()) {
            validatePassword(request.password());
        }

        // validate query
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(
                        "Data not found",
                        "DATA_NOT_FOUND"));

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BadRequestException("Email is already exists");
        }

        Role role = roleRepository.findByIdAndDeletedAtIsNull(request.role_id())
                .orElseThrow(() -> new BadRequestException("Invalid role id"));

        // updating data
        user.setRole(role);
        user.setFullname(request.fullname());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRemark(request.remark());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(
                        "Data not found",
                        "DATA_NOT_FOUND"));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

}
