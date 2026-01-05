package com.openclassrooms.mddapi.auth;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.config.AuthProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshCookieService {

	private final AuthProperties authProperties;

	/**
	 * Builds the HttpOnly refresh token cookie.
	 *
	 * @param refreshToken refresh token in plaintext (cookie value)
	 * @return a {@link ResponseCookie} ready to be sent via {@code Set-Cookie}
	 */
	public ResponseCookie buildRefreshCookie(String refreshToken) {
		return baseCookieBuilder(refreshToken)
				.maxAge(Duration.ofDays(authProperties.refreshTokenTtlDays()))
				.build();
	}

	/**
	 * Builds a refresh token cookie that clears the value in the browser.
	 *
	 * @return a {@link ResponseCookie} with {@code Max-Age=0}
	 */
	public ResponseCookie clearRefreshCookie() {
		return baseCookieBuilder("")
				.maxAge(Duration.ZERO)
				.build();
	}

	/**
	 * Creates a base cookie builder applying security attributes (HttpOnly, Secure, SameSite, Path).
	 *
	 * @param value cookie value
	 * @return a {@link ResponseCookie.ResponseCookieBuilder} for further customization
	 */
	private ResponseCookie.ResponseCookieBuilder baseCookieBuilder(String value) {
		return ResponseCookie.from(authProperties.refreshCookieName(), value)
				.httpOnly(true)
				.secure(authProperties.cookieSecure())
				.path(authProperties.cookiePath())
				.sameSite(authProperties.cookieSameSite());
	}
}
