package com.openclassrooms.mddapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
		String jwtSecret,
		long accessTokenTtlSeconds,
		int refreshTokenTtlDays,
		String refreshCookieName,
		boolean cookieSecure,
		String cookieSameSite,
		String cookiePath
) {
}
