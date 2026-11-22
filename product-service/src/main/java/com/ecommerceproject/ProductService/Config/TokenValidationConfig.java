package com.ecommerceproject.ProductService.Config;

import com.ecommerceproject.common.security.TokenValidationClient;
import com.ecommerceproject.common.security.TokenValidationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the shared token-validation filter into the Product Service so that
 * mutating product requests are authenticated against the User Service.
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
                new FilterRegistrationBean<>(new TokenValidationFilter(tokenValidationClient));
        registration.addUrlPatterns("/products", "/products/*");
        registration.setOrder(1);
        return registration;
    }
}
