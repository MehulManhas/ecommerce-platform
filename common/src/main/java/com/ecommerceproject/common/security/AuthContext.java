package com.ecommerceproject.common.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolved identity of the caller for the current request. The
 * {@link TokenValidationFilter} stores an instance under {@link #ATTRIBUTE} as a
 * request attribute; controllers can read it to make authorisation decisions.
 */
public class AuthContext {
    public static final String ATTRIBUTE = "com.ecommerceproject.common.security.AuthContext";

    private final Long userId;
    private final List<String> roles;

    public AuthContext(Long userId, List<String> roles) {
        this.userId = userId;
        this.roles = roles != null ? roles : new ArrayList<>();
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
