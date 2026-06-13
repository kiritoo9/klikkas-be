package com.klikkas.dto.users;

import java.util.List;

public record UserListResponse(
    List<UserResponse> data,
    Integer page,
    Integer totalPage
) {}