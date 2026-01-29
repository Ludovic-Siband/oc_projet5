package com.openclassrooms.mddapi.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.openclassrooms.mddapi.exception.UnauthorizedException;

/**
 * Extracts the authenticated user id from a JWT.
 */
@Component
public class CurrentUserService {

	/**
	 * Parses the user id from the JWT subject.
	 *
	 * @param jwt the authenticated JWT (may be null)
	 * @return the user id
	 * @throws UnauthorizedException if the token is missing or invalid
	 */
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
