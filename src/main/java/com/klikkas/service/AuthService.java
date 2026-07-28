package com.klikkas.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.klikkas.data.OrderCategories;
import com.klikkas.dto.auth.LoginRequest;
import com.klikkas.dto.auth.LoginResponse;
import com.klikkas.dto.auth.RefreshTokenRequest;
import com.klikkas.dto.auth.RegistRequest;
import com.klikkas.dto.auth.UserLoginResponse;
import com.klikkas.dto.order_categories.CategoryAccountTemplate;
import com.klikkas.dto.users.UserResponse;
import com.klikkas.entity.Account;
import com.klikkas.entity.OrderCategory;
import com.klikkas.entity.Packages;
import com.klikkas.entity.Role;
import com.klikkas.entity.Tenant;
import com.klikkas.entity.User;
import com.klikkas.entity.UserAgent;
import com.klikkas.entity.UserPackage;
import com.klikkas.entity.UserTenant;
import com.klikkas.entity.UserToken;
import com.klikkas.exception.BadRequestException;
import com.klikkas.exception.NotFoundException;
import com.klikkas.repository.AccountRepository;
import com.klikkas.repository.OrderCategoryRepository;
import com.klikkas.repository.PackageRepository;
import com.klikkas.repository.RoleRepository;
import com.klikkas.repository.TenantRepository;
import com.klikkas.repository.UserAgentRepository;
import com.klikkas.repository.UserPackageRepository;
import com.klikkas.repository.UserRepository;
import com.klikkas.repository.UserTenantRepository;
import com.klikkas.repository.UserTokenRepository;
import com.klikkas.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
    public final AccountRepository accountRepository;
    public final OrderCategoryRepository categoryRepository;
    public final PackageRepository packageRepository;
    public final UserPackageRepository userPackageRepository;
    public final UserAgentRepository userAgentRepository;
    public final UserTokenRepository userTokenRepository;

    public final JwtService jwtService;
    public final PasswordEncoder passwordEncoder;

    private GoogleIdToken.Payload verifyGoogleToken(String googleID) {
        if (googleID == null || googleID.isBlank()) {
            return null;
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .build();
            GoogleIdToken idToken = verifier.verify(googleID);
            if (idToken != null) {
                return idToken.getPayload();
            }
            return null;
        } catch (Exception e) {
            System.out.println("Google OAuth verification failed: " + e.getMessage());
            return null;
        }
    }

    public LoginResponse login(LoginRequest request) {
        User user;

        // check if user login using google.id_token
        if (request.google_id() != null && !request.google_id().isBlank()) {
            GoogleIdToken.Payload payload = verifyGoogleToken(request.google_id());
            if (payload == null) {
                throw new BadRequestException("Invalid Google ID token");
            }
            // Get the 'sub' claim from verified token
            String googleSub = payload.getSubject();
            // Find user by googleSub (sub claim)
            user = userRepository.findByGoogleIdAndDeletedAtIsNull(googleSub)
                    .orElseThrow(() -> new NotFoundException(
                            "User not found with this Google account",
                            "INVALID_CREDENTIAL"));
        } else {
            // Regular email/password login
            user = userRepository
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
        }

        // get user tenant
        UserTenant userTenant = userTenantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Invalid credential"));

        UUID tenantID = userTenant.getTenant().getId();

        // check available user package
        // if not exists, create free tier package for this user
        Optional<UserPackage> existsUserPackage = userPackageRepository.findByUserIdAndDeletedAtIsNull(user.getId());
        if (!existsUserPackage.isPresent()) {
            Packages pack = packageRepository.findByCodeAndDeletedAtIsNull("STR1")
                .orElseThrow(() -> new BadRequestException("Something went wrong"));

            // create default value for user package
            UserPackage userPackage = new UserPackage();
            userPackage.setUser(user);
            userPackage.setPackages(pack);
            userPackage.setIs_active(true);
            userPackage.setRemark("Free tier");
            userPackage.setStart_at(LocalDateTime.now());
            userPackage = userPackageRepository.save(userPackage);

            // create default value for user agent and user token
            UserAgent userAgent = new UserAgent();
            userAgent.setUser(user);
            userAgent.setName("Personal Assistant");
            userAgent.setShortname("-");
            userAgent.setLang("id-ID");
            userAgent.setTone("professional");
            userAgent = userAgentRepository.save(userAgent);

            // create default value for user token
            UserToken userToken = new UserToken();
            userToken.setUser(user);
            userToken.setLimit_token(1_000_000);
            userToken.setUsage_token(0);
            userToken.setReset_at(LocalDateTime.now().plusMonths(1));
            userToken = userTokenRepository.save(userToken);
        }

        // generate token
        String accessToken = jwtService.generateToken(user, tenantID, 1, "access");
        String refreshToken = jwtService.generateToken(user, tenantID, 14, "refresh");

        UserLoginResponse userLogin = new UserLoginResponse(
                user.getEmail(),
                user.getFullname(),
                user.getRole().getName());

        return new LoginResponse(
                accessToken,
                refreshToken,
                userLogin);
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // validate token
        if (!jwtService.validateToken(request.refresh_token())) {
            throw new BadRequestException("Invalid credential");
        }

        // check token type
        String tokenType = jwtService.extractTokenType(request.refresh_token());
        if (!"refresh".equals(tokenType)) {
            throw new BadRequestException("Invalid credential");
        }

        // extract user info from token
        String email = jwtService.extractEmail(request.refresh_token());
        UUID tenantID = jwtService.extractTenantID(request.refresh_token());

        // find user
        User user = userRepository
                .findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BadRequestException("Invalid credential"));

        // generate new tokens
        String accessToken = jwtService.generateToken(user, tenantID, 1, "access");
        String newRefreshToken = jwtService.generateToken(user, tenantID, 14, "refresh");

        UserLoginResponse userLogin = new UserLoginResponse(
                user.getEmail(),
                user.getFullname(),
                user.getRole().getName());

        return new LoginResponse(
                accessToken,
                newRefreshToken,
                userLogin);
    }

    @Transactional
    public UserResponse regist(RegistRequest req) {
        String googleSub = null;
        
        // Check if google_id is provided for OAuth registration
        if (req.google_id() != null && !req.google_id().isBlank()) {
            // Verify Google ID token and extract the 'sub' claim
            GoogleIdToken.Payload payload = verifyGoogleToken(req.google_id());
            if (payload == null) {
                throw new BadRequestException("Invalid Google ID token");
            }
            
            // Get the 'sub' claim (unique Google user ID)
            googleSub = payload.getSubject();
            
            // For Google OAuth, verify email in the token matches the request
            String googleEmail = payload.getEmail();
            if (!googleEmail.equals(req.email())) {
                throw new BadRequestException("Email mismatch with Google account");
            }
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new BadRequestException("Email already taken");
        }

        Role role = roleRepository.findByNameAndDeletedAtIsNull("user")
                .orElseThrow(() -> new BadRequestException("Something went wrong"));

        Packages pack = packageRepository.findByCodeAndDeletedAtIsNull("STR1")
                .orElseThrow(() -> new BadRequestException("Something went wrong"));

        // prepare data
        User user = new User();

        user.setEmail(req.email());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFullname(req.fullname());
        user.setPhone(req.phone());
        user.setAddress(req.address());
        // Store the 'sub' claim (Google user ID) instead of the id_token
        user.setGoogleId(googleSub);
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

        // create default value for user package
        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setPackages(pack);
        userPackage.setIs_active(true);
        userPackage.setRemark("Free tier");
        userPackage.setStart_at(LocalDateTime.now());
        userPackage = userPackageRepository.save(userPackage);

        // create default value for user agent and user token
        UserAgent userAgent = new UserAgent();
        userAgent.setUser(user);
        userAgent.setName("Personal Assistant");
        userAgent.setShortname("-");
        userAgent.setLang("id-ID");
        userAgent.setTone("professional");
        userAgent = userAgentRepository.save(userAgent);

        // create default value for user token
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setLimit_token(1_000_000);
        userToken.setUsage_token(0);
        userToken.setReset_at(LocalDateTime.now().plusMonths(1));
        userToken = userTokenRepository.save(userToken);

        // add default data for order category
        List<OrderCategory> categories = new ArrayList<>();
        List<CategoryAccountTemplate> templates = OrderCategories.get();

        for (CategoryAccountTemplate t : templates) {
            if (t.debitAccountCode() == "" || t.creditAccountCode() == "") {
                continue;
            }

            OrderCategory c = new OrderCategory();
            c.setTenant(tenant);

            Account debitAccount = accountRepository.findByCodeAndDeletedAtIsNull(t.debitAccountCode())
                    .orElseThrow(() -> new BadRequestException("Invalid debit account"));
            c.setDebitAccount(debitAccount);

            Account creditAccount = accountRepository.findByCodeAndDeletedAtIsNull(t.creditAccountCode())
                    .orElseThrow(() -> new BadRequestException("Invalid credit account"));
            c.setCreditAccount(creditAccount);

            c.setName(t.name());
            c.setCategoryType(t.categoryType());
            c.setDescription(t.description());
            c.setIsActive(true);

            categories.add(c);
        }
        categoryRepository.saveAll(categories);

        // response data
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
