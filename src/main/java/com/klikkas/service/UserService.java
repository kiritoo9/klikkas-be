package com.klikkas.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.klikkas.dto.users.UserListResponse;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.entity.User;
import com.klikkas.exception.NotFoundException;
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
                new NotFoundException("User not found")
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

}
