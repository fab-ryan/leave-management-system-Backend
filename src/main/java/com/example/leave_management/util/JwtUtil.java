package com.example.leave_management.util;

import com.example.leave_management.model.Employee;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final SecretKey key;

    @Value("${security.jwt.expiration}")
    private Long expiration;

    @Value("${security.jwt.secret}")
    private String secret;

    public byte[] getSecretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public JwtUtil() {
        // Use a fixed key from properties
        this.key = Keys.hmacShaKeyFor("3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b".getBytes());
    }

    public String generateToken(Employee employee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", employee.getRole().name());
        claims.put("userId", employee.getId());
        claims.put("email", employee.getEmail());

        return createToken(claims, employee.getEmail().toString());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            // First validate the token structure and expiration
            if (isTokenExpired(token)) {
                return false;
            }

            // If userDetails is provided, validate against it
            if (userDetails != null) {
                final String username = extractUsername(token);
                return username.equals(userDetails.getUsername());
            }

            // If no userDetails provided, just validate token structure
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class); // Extract the userId claim
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getLoggedUserId(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return extractUserId(token); // Extract "userId" from the token
        } else {
            throw new IllegalStateException("Authorization header is missing or invalid");
        }
    }

}