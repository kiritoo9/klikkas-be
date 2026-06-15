package com.klikkas.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.klikkas.dto.roles.RoleListResponse;
import com.klikkas.dto.roles.RoleResponse;
import com.klikkas.entity.Role;
import com.klikkas.repository.RoleRepository;
import com.klikkas.specification.RoleSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

        private final RoleRepository roleRepository;

        public RoleListResponse getRoles(
                        Integer page,
                        Integer limit,
                        String order,
                        String dir,
                        String keywords) {
                Pageable pageable;

                if (order != null && !order.isBlank()) {
                        Sort sort = dir.equalsIgnoreCase("desc")
                                        ? Sort.by(order).descending()
                                        : Sort.by(order).ascending();

                        pageable = PageRequest.of(page - 1, limit, sort);
                } else {
                        pageable = PageRequest.of(page - 1, limit);
                }

                Specification<Role> spec = Specification
                                .where(RoleSpecification.notdeleted())
                                .and(RoleSpecification.notRoot())
                                .and(RoleSpecification.keyword(keywords));

                Page<Role> result = roleRepository.findAll(
                                spec,
                                pageable);

                List<RoleResponse> roles = result.getContent().stream().map(role -> new RoleResponse(
                                role.getId(),
                                role.getName(),
                                role.getDescription(),
                                role.getCreatedAt())).toList();

                return new RoleListResponse(
                                roles,
                                page,
                                result.getTotalPages());
        }

}
