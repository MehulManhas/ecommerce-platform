package com.ecommerceproject.UserService.Dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Returned by /auth/login. The token is in the body (not only a cookie) so a
 * browser SPA can store it and send it as an Authorization: Bearer header.
 */
@Getter
@Setter
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String email;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, Long userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }
}
