package com.lms.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Handles JWT token generation and validation. This service manages JWT token
 * creation, signature verification, subject extraction, and token expiration
 * for authentication and authorization in the LMS platform.
 *
 * @author VisionWaves
 * @version 1.0
 */
public class JwtService {

    /**
     * Secret key for JWT signing
     */
    private final Key key;

    /**
     * Token expiration time in milliseconds
     */
    private final long expirationMs;

    /**
     * Constructs a JWT service with the provided secret and expiration.
     *
     * @param base64Secret the base64-encoded secret key
     * @param expirationMs the token expiration time in milliseconds
     */
    public JwtService(String base64Secret, long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given subject.
     *
     * @param subject the subject (typically user ID or email)
     * @return the generated JWT token string
     */
    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the subject from a JWT token.
     *
     * @param token the JWT token string
     * @return the subject extracted from the token
     */
    public String extractSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}



