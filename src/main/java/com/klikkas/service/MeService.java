package com.klikkas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klikkas.dto.users.ChangePasswordRequest;
import com.klikkas.dto.users.UpdateProfileRequest;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.entity.User;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.UserRepository;
import com.klikkas.security.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getMe() {
        String email = TenantContext.getEmail();
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("User not found", "USER_NOT_FOUND"));

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

    public UserResponse updateProfile(UpdateProfileRequest req) {
        String email = TenantContext.getEmail();
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("User not found", "USER_NOT_FOUND"));

        user.setFullname(req.fullname());
        if (req.phone() != null) {
            user.setPhone(req.phone());
        }
        if (req.address() != null) {
            user.setAddress(req.address());
        }
        if (req.remark() != null) {
            user.setRemark(req.remark());
        }
        user = userRepository.save(user);

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

    public void changePassword(ChangePasswordRequest req) {
        String email = TenantContext.getEmail();
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("User not found", "USER_NOT_FOUND"));

        // Verify old password
        if (!passwordEncoder.matches(req.old_password(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Verify new password and confirm password match
        if (!req.new_password().equals(req.confirm_password())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Verify new password is not same as old password
        if (req.old_password().equals(req.new_password())) {
            throw new BadRequestException("New password must be different from old password");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(req.new_password()));
        userRepository.save(user);
    }

}