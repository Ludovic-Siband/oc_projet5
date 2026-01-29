package com.openclassrooms.mddapi.feature.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.openclassrooms.mddapi.config.AuthProperties;

class RefreshTokenServiceTest {

	@Test
	void generateTokenIsUrlSafe() {
		RefreshTokenService service = new RefreshTokenService(defaultProps());

		String token = service.generateToken();

		assertThat(token).isNotBlank();
		assertThat(token).doesNotContain("=");
	}

	@Test
	void hashTokenIsDeterministic() {
		RefreshTokenService service = new RefreshTokenService(defaultProps());

		String first = service.hashToken("token");
		String second = service.hashToken("token");

		assertThat(first).isEqualTo(second);
	}

	@Test
	void computeExpiresAtUsesConfiguredDays() {
		RefreshTokenService service = new RefreshTokenService(defaultProps());
		Instant start = Instant.now();

		Instant expiresAt = service.computeExpiresAt();

		assertThat(expiresAt).isAfter(start);
		assertThat(ChronoUnit.DAYS.between(start, expiresAt)).isEqualTo(1);
	}

	private AuthProperties defaultProps() {
		return new AuthProperties("0123456789abcdef0123456789abcdef", 900, 1, "refreshToken", false, "Strict", "/");
	}
}
