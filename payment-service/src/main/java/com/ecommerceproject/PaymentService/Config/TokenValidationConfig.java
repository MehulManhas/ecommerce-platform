package com.ecommerceproject.PaymentService.Config;

import com.ecommerceproject.common.security.TokenValidationClient;
import com.ecommerceproject.common.security.TokenValidationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authenticates payment requests against the User Service using the shared
 * token-validation filter, so only signed-in callers can submit a payment.
 */
@Configuration
public class TokenValidationConfig {

    @Bean
    public TokenValidationClient tokenValidationClient(
            @Value("${userservice.base-url:http://localhost:8081}") String userServiceBaseUrl) {
        return new TokenValidationClient(userServiceBaseUrl);
    }

    @Bean
    public FilterRegistrationBean<TokenValidationFilter> tokenValidationFilterRegistration(
            TokenValidationClient tokenValidationClient) {
        FilterRegistrationBean<TokenValidationFilter> registration =
                new FilterRegistrationBean<>(new TokenValidationFilter(tokenValidationClient, true));
        registration.addUrlPatterns("/payments", "/payments/*");
        registration.setOrder(1);
        return registration;
    }
}
