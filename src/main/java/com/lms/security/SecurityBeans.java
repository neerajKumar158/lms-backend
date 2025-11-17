package com.lms.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityBeans {

    @Bean
    JwtService jwtService(@Value("${security.jwt.secret}") String secret,
                          @Value("${security.jwt.expiration-ms:86400000}") long expMs) {
        return new JwtService(secret, expMs);
    }
}



