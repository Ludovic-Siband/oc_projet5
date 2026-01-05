package com.openclassrooms.mddapi.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import com.openclassrooms.mddapi.config.AuthProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

	/**
	 * Builds the HMAC secret key used to sign and verify JWTs.
	 *
	 * @param authProperties application auth configuration
	 * @return a {@link SecretKey} suitable for HS256
	 * @throws IllegalStateException if the configured secret is too short
	 */
	@Bean
	public SecretKey jwtSecretKey(AuthProperties authProperties) {
		byte[] keyBytes = authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
		}
		return new SecretKeySpec(keyBytes, "HmacSHA256");
	}

	/**
	 * Provides a JWT encoder for issuing signed access tokens.
	 *
	 * @param jwtSecretKey HS256 secret key
	 * @return a {@link JwtEncoder}
	 */
	@Bean
	public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	/**
	 * Provides a JWT decoder for validating incoming bearer tokens.
	 *
	 * @param jwtSecretKey HS256 secret key
	 * @return a {@link JwtDecoder}
	 */
	@Bean
	public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
		return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
	}
}
