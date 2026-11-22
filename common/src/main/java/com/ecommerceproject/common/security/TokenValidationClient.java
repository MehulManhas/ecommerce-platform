package com.ecommerceproject.common.security;

import com.ecommerceproject.common.dto.ValidateTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls the User Service /auth/validate endpoint to resolve and verify a caller's
 * JWT. Any transport/HTTP failure (including a 401 from the User Service) is
 * translated into an invalid response rather than propagating an exception.
 */
public class TokenValidationClient {

    private final RestClient restClient;

    public TokenValidationClient(String userServiceBaseUrl) {
        this.restClient = RestClient.create(userServiceBaseUrl);
    }

    public ValidateTokenResponse validate(String bearerToken) {
        try {
            ValidateTokenResponse response = restClient.get()
                    .uri("/auth/validate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(ValidateTokenResponse.class);
            return response != null ? response : invalid();
        } catch (RestClientException ex) {
            return invalid();
        }
    }

    private ValidateTokenResponse invalid() {
        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setValid(false);
        return response;
    }
}
