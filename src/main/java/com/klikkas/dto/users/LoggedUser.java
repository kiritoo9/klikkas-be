package com.klikkas.dto.users;

import java.util.UUID;

public record LoggedUser(
        String email,
        UUID tenantID) {

}
