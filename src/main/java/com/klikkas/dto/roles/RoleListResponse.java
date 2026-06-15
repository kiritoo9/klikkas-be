package com.klikkas.dto.roles;

import java.util.List;

public record RoleListResponse(
    List<RoleResponse> data,
    Integer page,
    Integer totalPage
) {}