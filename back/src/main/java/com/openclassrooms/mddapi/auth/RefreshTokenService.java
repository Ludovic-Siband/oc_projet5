package com.openclassrooms.mddapi.auth;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.config.AuthProperties;

@Service
public class RefreshTokenService {

	private final AuthProperties authProperties;
	private final SecureRandom secureRandom = new SecureRandom();

	public RefreshTokenService(AuthProperties authProperties) {
		this.authProperties = authProperties;
	}

	/**
	 * Generates a new refresh token suitable for storage in an HttpOnly cookie.
	 *
	 * @return a URL-safe Base64-encoded random token
	 */
	public String generateToken() {
		byte[] bytes = new byte[64];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/**
	 * Computes a server-side fingerprint of a refresh token using HMAC-SHA256.
	 * <p>
	 * The returned value is safe to store in the database. The refresh token itself must never be stored in plaintext.
	 *
	 * @param token the refresh token in plaintext (as received from the client cookie)
	 * @return a URL-safe Base64-encoded HMAC digest
	 * @throws IllegalStateException if the hashing operation fails
	 */
	public String hashToken(String token) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] digest = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		} catch (Exception ex) {
			throw new IllegalStateException("Impossible de chiffrer le token de rafraîchissement", ex);
		}
	}

	/**
	 * Computes the expiration instant for a newly issued refresh token based on configuration.
	 *
	 * @return the expiry {@link Instant} in UTC
	 */
	public Instant computeExpiresAt() {
		return Instant.now().plus(authProperties.refreshTokenTtlDays(), ChronoUnit.DAYS);
	}
}
