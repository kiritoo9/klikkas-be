package com.klikkas.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;

import com.klikkas.entity.User;
import com.klikkas.dto.users.CreateUserRequest;
import com.klikkas.dto.users.UserListResponse;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.repository.UserRepository;

@Service
public class UserService {

    public final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public UserListResponse getUsers(
        Integer page,
        Integer limit,
        String order,
        String dir
    ) {
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
        Page<User> result = userRepository.findByDeletedAtIsNull(pageable);
        List<UserResponse> users = result.getContent().stream().map(user -> new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullname(),
            user.getPhone(),
            user.getAddress(),
            user.getRemark(),
            user.getCreatedAt()
        )).toList();

        return new UserListResponse(
            users,
            page,
            result.getTotalPages()
        );
    }

    public UserResponse getUser(UUID id) {
        User user = userRepository
            .findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> 
                new NotFoundException(
                    "User not found",
                    "USER_NOT_FOUND"
                )
            );

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullname(),
            user.getPhone(),
            user.getAddress(),
            user.getRemark(),
            user.getCreatedAt()
        );
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFullname(request.fullname());

        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRemark(request.remark());

        User saved = userRepository.save(user);

        return new UserResponse(
            saved.getId(),
            saved.getEmail(),
            saved.getFullname(),
            saved.getPhone(),
            saved.getAddress(),
            saved.getRemark(),
            saved.getCreatedAt()
        );
    }

}
