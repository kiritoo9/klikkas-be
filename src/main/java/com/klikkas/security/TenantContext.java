package com.klikkas.security;

import java.util.UUID;

import com.klikkas.dto.users.LoggedUser;

public class TenantContext {

    private static final ThreadLocal<LoggedUser> CURRENT_TENANT = new ThreadLocal<>();

    public static void setContext(String email, UUID tenantID) {
        CURRENT_TENANT.set(new LoggedUser(
                email,
                tenantID));
    }

    public static UUID getTenantId() {
        LoggedUser logged = CURRENT_TENANT.get();
        return logged.tenantID();
    }

    public static String getEmail() {
        LoggedUser logged = CURRENT_TENANT.get();
        return logged.email();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

}
