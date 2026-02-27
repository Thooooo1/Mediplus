package com.example.medibook.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

  private final Key key;
  private final long expireMinutes;

  public JwtService(
      @Value("${app.security.jwtSecret}") String secret,
      @Value("${app.security.jwtExpireMinutes}") long expireMinutes
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expireMinutes = expireMinutes;
  }

  public String generateToken(String userId, String email, String role) {
    Instant now = Instant.now();
    Instant exp = now.plus(expireMinutes, ChronoUnit.MINUTES);

    return Jwts.builder()
      .subject(userId)
      .claims(Map.of("email", email, "role", role))
      .issuedAt(Date.from(now))
      .expiration(Date.from(exp))
      .signWith(key)
      .compact();
  }

  public boolean isValid(String token) {
    try {
      Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String extractUserId(String token) {
    return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build()
      .parseSignedClaims(token).getPayload().getSubject();
  }
}
