package com.klikkas.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.user_token.TokenUsageList;
import com.klikkas.dto.user_token.TokenUsages;
import com.klikkas.dto.user_token.UserSummary;
import com.klikkas.entity.UserToken;
import com.klikkas.entity.UserTokenUsages;
import com.klikkas.repository.UserTokenRepository;
import com.klikkas.repository.UserTokenUsageRepository;
import com.klikkas.security.TenantContext;
import com.klikkas.specification.UserTokenUsageSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserTokenService {

    private final UserTokenRepository userTokenRepository;
    private final UserTokenUsageRepository userTokenUsageRepository;

    public UserSummary getSummaryToken() {
        UUID userID = TenantContext.getUserId();

        Integer limitToken = 0;
        Integer usageToken = 0;
        Double estimatedCost = 0.0;
        LocalDateTime resetAt = null;

        UserToken userToken = userTokenRepository.findByUserId(userID);
        if (userToken != null) {
            limitToken = userToken.getLimit_token();
            usageToken = userToken.getUsage_token();
            resetAt = userToken.getReset_at();
            estimatedCost = calculateEstimatedCost(usageToken);
        }

        return new UserSummary(
                limitToken,
                usageToken,
                estimatedCost,
                resetAt);
    }

    public TokenUsageList getTokenUsages(
            Integer page,
            Integer limit,
            String order,
            String dir,
            String keywords) {
        UUID userID = TenantContext.getUserId();

        Pageable pageable;

        if (order != null && !order.isBlank()) {
            Sort sort = dir.equalsIgnoreCase("desc")
                    ? Sort.by(order).descending()
                    : Sort.by(order).ascending();

            pageable = PageRequest.of(page - 1, limit, sort);
        } else {
            pageable = PageRequest.of(page - 1, limit);
        }

        Specification<UserTokenUsages> spec = Specification
                .where(UserTokenUsageSpecification.byUser(userID))
                .and(UserTokenUsageSpecification.keywords(keywords));

        Page<UserTokenUsages> result = userTokenUsageRepository.findAll(
                spec,
                pageable);

        List<TokenUsages> tokenUsages = result.getContent().stream().map(usage -> {
            Double estimatedCost = calculateEstimatedCost(usage.getInput_token() + usage.getOutput_token());

            return new TokenUsages(
                    usage.getUsage_title(),
                    usage.getLlm_model(),
                    usage.getInput_token(),
                    usage.getOutput_token(),
                    estimatedCost,
                    usage.getCreatedAt());
        }).toList();

        return new TokenUsageList(
                tokenUsages,
                page,
                result.getTotalPages());
    }

    private Double calculateEstimatedCost(Integer usageToken) {
        // calculate estimate
        // use static price for now
        Integer maxToken = 150_000_000;
        Integer cost = 4 * 18_000; // USD * IDR
        Double costPerToken = (double) cost / maxToken;

        return usageToken * costPerToken;
    }
}
