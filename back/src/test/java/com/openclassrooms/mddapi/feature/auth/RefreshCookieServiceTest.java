package com.openclassrooms.mddapi.feature.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.openclassrooms.mddapi.config.AuthProperties;

class RefreshCookieServiceTest {

	@Test
	void buildRefreshCookieAppliesSecuritySettings() {
		AuthProperties props = new AuthProperties("0123456789abcdef0123456789abcdef", 900, 2, "refreshToken", true, "Strict", "/api");
		RefreshCookieService service = new RefreshCookieService(props);

		ResponseCookie cookie = service.buildRefreshCookie("token");

		assertThat(cookie.getName()).isEqualTo("refreshToken");
		assertThat(cookie.getValue()).isEqualTo("token");
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.isSecure()).isTrue();
		assertThat(cookie.getPath()).isEqualTo("/api");
		assertThat(cookie.getSameSite()).isEqualTo("Strict");
		assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(2));
	}

	@Test
	void clearRefreshCookieZeroesMaxAge() {
		AuthProperties props = new AuthProperties("0123456789abcdef0123456789abcdef", 900, 2, "refreshToken", false, "Lax", "/");
		RefreshCookieService service = new RefreshCookieService(props);

		ResponseCookie cookie = service.clearRefreshCookie();

		assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
		assertThat(cookie.getValue()).isEmpty();
	}
}
