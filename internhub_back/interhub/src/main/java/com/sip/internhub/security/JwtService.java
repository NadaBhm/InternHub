package com.sip.internhub.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

	private final SecretKey signingKey;
	private final long expirationMs;

	public JwtService(@Value("${app.jwt.secret:}") String configuredSecret,
			@Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
		if (expirationMs <= 0) {
			throw new IllegalArgumentException("app.jwt.expiration-ms must be positive");
		}
		this.expirationMs = expirationMs;
		if (configuredSecret == null || configuredSecret.isBlank()) {
			this.signingKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
			logger.warn("INTERNHUB_JWT_SECRET is unset; using an ephemeral development key. Tokens will expire on restart.");
		} else {
			byte[] secretBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
			if (secretBytes.length < 32) {
				throw new IllegalArgumentException("INTERNHUB_JWT_SECRET must contain at least 32 UTF-8 bytes");
			}
			this.signingKey = Keys.hmacShaKeyFor(secretBytes);
		}
	}

	public String generateToken(String email) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(signingKey)
				.compact();
	}

	public String extractEmail(String token) {
		return claims(token).getSubject();
	}

	public boolean isTokenValid(String token, String email) {
		Claims claims = claims(token);
		return email.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
