package com.ecommerceproject.common.security;

import com.ecommerceproject.common.dto.ValidateTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates the caller's bearer token against the User Service for mutating
 * requests (POST/PUT/PATCH/DELETE). Read requests (GET) pass through so the
 * catalogue stays publicly browsable. On success the resolved {@link AuthContext}
 * is attached to the request; on failure a 401 is returned.
 */
public class TokenValidationFilter extends OncePerRequestFilter {

    private final TokenValidationClient tokenValidationClient;

    public TokenValidationFilter(TokenValidationClient tokenValidationClient) {
        this.tokenValidationClient = tokenValidationClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!requiresAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            unauthorized(response, "Missing bearer token");
            return;
        }

        String token = authorization.substring("Bearer ".length());
        ValidateTokenResponse result = tokenValidationClient.validate(token);

        if (result == null || !result.isValid()) {
            unauthorized(response, "Invalid or expired token");
            return;
        }

        request.setAttribute(AuthContext.ATTRIBUTE, new AuthContext(result.getUserId(), result.getRoles()));
        filterChain.doFilter(request, response);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + message + "\"}");
    }
}
