package com.klikkas.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klikkas.dto.auth.LoginRequest;
import com.klikkas.dto.auth.LoginResponse;
import com.klikkas.dto.auth.RegistRequest;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.entity.Role;
import com.klikkas.entity.Tenant;
import com.klikkas.entity.User;
import com.klikkas.entity.UserTenant;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.RoleRepository;
import com.klikkas.repository.TenantRepository;
import com.klikkas.repository.UserRepository;
import com.klikkas.repository.UserTenantRepository;
import com.klikkas.security.JwtService;
import com.klikkas.util.RandomString;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final UserRepository userRepository;
    public final RoleRepository roleRepository;
    public final TenantRepository tenantRepository;
    public final UserTenantRepository userTenantRepository;

    public final JwtService jwtService;
    public final PasswordEncoder passwordEncoder;

    private Boolean checkGoogleOAuth(String idToken) {
        return false;
    }

    public LoginResponse login(LoginRequest request) {
        // check if user login using google.id_token
        if (request.id_token() != "" || !request.id_token().isBlank()) {
            Boolean isOAuth = checkGoogleOAuth(request.id_token());
            System.out.println("User login with OAuth: " + isOAuth);
        }

        User user = userRepository
                .findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new NotFoundException(
                        "Invalid credential",
                        "INVALID_CREDENTIAL"));

        boolean validPassword = passwordEncoder.matches(
                request.password(),
                user.getPassword());

        if (!validPassword) {
            throw new BadRequestException("Invalid credential");
        }

        // get user tenant
        UserTenant userTenant = userTenantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Invalid credential"));

        UUID tenantID = userTenant.getTenant().getId();

        // generate token
        String accessToken = jwtService.generateToken(user, tenantID, 1);
        String refreshToken = jwtService.generateToken(user, tenantID, 14);

        return new LoginResponse(
                accessToken,
                refreshToken);
    }

    @Transactional
    public UserResponse regist(RegistRequest req) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new BadRequestException("Email already taken");
        }

        Role role = roleRepository.findByNameAndDeletedAtIsNull("user")
                .orElseThrow(() -> new BadRequestException("Something went wrong"));

        // prepare data
        User user = new User();

        user.setEmail(req.email());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFullname(req.fullname());
        user.setPhone(req.phone());
        user.setAddress(req.address());
        user = userRepository.save(user);

        Tenant tenant = new Tenant();

        tenant.setCode(RandomString.randomString(16));
        tenant.setName("TenantOf-" + req.fullname());
        tenant.setDescription("created-automatically");
        tenant.setRemark("created-automatically");
        tenant.setIsActive(true);
        tenant = tenantRepository.save(tenant);

        UserTenant userTenant = new UserTenant();

        userTenant.setUser(user);
        userTenant.setTenant(tenant);
        userTenant.setRemark("created-automatically");
        userTenant = userTenantRepository.save(userTenant);

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

}
