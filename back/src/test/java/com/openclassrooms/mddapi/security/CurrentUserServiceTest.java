package com.openclassrooms.mddapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import com.openclassrooms.mddapi.exception.UnauthorizedException;

class CurrentUserServiceTest {

	private final CurrentUserService currentUserService = new CurrentUserService();

	@Test
	void getUserIdThrowsOnMissingJwt() {
		assertThatThrownBy(() -> currentUserService.getUserId(null))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void getUserIdThrowsOnBlankSubject() {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(" ").build();

		assertThatThrownBy(() -> currentUserService.getUserId(jwt))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void getUserIdThrowsOnNonNumericSubject() {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("abc").build();

		assertThatThrownBy(() -> currentUserService.getUserId(jwt))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void getUserIdParsesNumericSubject() {
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("42").build();

		assertThat(currentUserService.getUserId(jwt)).isEqualTo(42L);
	}
}
