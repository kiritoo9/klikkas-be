package com.klikkas.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.klikkas.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

        @Value("${jwt.secret}")
        private String secret;

        public String generateToken(
                        User user,
                        UUID tenantID,
                        Integer expiration,
                        String tokenType) {

                SecretKey key = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                return Jwts.builder()
                                .subject(user.getEmail())
                                .claim("id", user.getId())
                                .claim("role", user.getRole() != null ? user.getRole().getName() : null)
                                .claim("tenantID", tenantID)
                                .claim("type", tokenType)
                                .issuedAt(new Date())
                                .expiration(
                                                new Date(System.currentTimeMillis() + (86400000L * expiration)))
                                .signWith(key)
                                .compact();

        }

        public boolean validateToken(String token) {
                try {
                        SecretKey key = Keys.hmacShaKeyFor(
                                        secret.getBytes(StandardCharsets.UTF_8));

                        Jwts.parser()
                                        .verifyWith(key)
                                        .build()
                                        .parseSignedClaims(token);

                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public String extractEmail(String token) {
                SecretKey key = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                return Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .getSubject();
        }

        public String extractRole(String token) {
                SecretKey key = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                return Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .get("role", String.class);
        }

        public UUID extractTenantID(String token) {
                SecretKey key = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                String tenantID = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .get("tenantID", String.class);

                return UUID.fromString(tenantID);
        }

        public UUID extractUserID(String token) {
                SecretKey key = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                String userID = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .get("id", String.class);

                return UUID.fromString(userID);
        }

        public String extractTokenType(String token) {
                SecretKey key = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));

                return Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload()
                                .get("type", String.class);
        }

}
