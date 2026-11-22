package com.ecommerceproject.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response returned by the User Service /auth/validate endpoint and consumed by
 * other services to authorise a caller. Plain POJO (no Lombok) so it can be
 * shared without pulling an annotation processor into every module.
 */
public class ValidateTokenResponse {
    private Long userId;
    private boolean valid;
    private List<String> roles = new ArrayList<>();

    public ValidateTokenResponse() {
    }

    public ValidateTokenResponse(Long userId, boolean valid, List<String> roles) {
        this.userId = userId;
        this.valid = valid;
        this.roles = roles != null ? roles : new ArrayList<>();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
