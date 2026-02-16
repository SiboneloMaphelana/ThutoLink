package com.backend.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public static final String CLAIM_NEW_USER = "newUser";

    public String generateToken(String subject) {
        return generateToken(subject, false);
    }

    public String generateToken(String subject, boolean newUser) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(exp);
        if (newUser) {
            builder.claim(CLAIM_NEW_USER, true);
        }
        return builder.signWith(key, Jwts.SIG.HS256).compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns true only if this token was issued at registration (sign-up), not at login.
     */
    public boolean isNewUserToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return Boolean.TRUE.equals(claims.get(CLAIM_NEW_USER, Boolean.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
