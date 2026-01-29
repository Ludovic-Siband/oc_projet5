package com.openclassrooms.mddapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.openclassrooms.mddapi.config.AuthProperties;

class JwtConfigTest {

	private final JwtConfig jwtConfig = new JwtConfig();

	@Test
	void jwtSecretKeyThrowsWhenTooShort() {
		AuthProperties props = new AuthProperties("short", 900, 1, "refresh", false, "Strict", "/");

		assertThatThrownBy(() -> jwtConfig.jwtSecretKey(props))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("at least 32 bytes");
	}

	@Test
	void jwtSecretKeyBuildsKey() {
		AuthProperties props = new AuthProperties("0123456789abcdef0123456789abcdef", 900, 1, "refresh", false, "Strict", "/");

		SecretKey key = jwtConfig.jwtSecretKey(props);

		assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
	}
}
