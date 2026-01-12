package com.openclassrooms.mddapi.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.openclassrooms.mddapi.exception.UnauthorizedException;

@Component
public class CurrentUserService {

	public long getUserId(Jwt jwt) {
		if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
			throw new UnauthorizedException("Utilisateur non authentifié");
		}
		try {
			return Long.parseLong(jwt.getSubject());
		} catch (NumberFormatException ex) {
			throw new UnauthorizedException("Utilisateur non authentifié");
		}
	}
}
