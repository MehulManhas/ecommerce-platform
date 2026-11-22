package com.ecommerceproject.ProductService.Config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching abstraction. With spring-boot-starter-data-redis on
 * the classpath and spring.cache.type=redis, Spring Boot auto-configures a
 * RedisCacheManager. Cached values (ProductResponseDto / ProductListResponseDto)
 * are Serializable, so the default JDK serializer round-trips them.
 *
 * To benchmark the uncached baseline, set spring.cache.type=none.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
