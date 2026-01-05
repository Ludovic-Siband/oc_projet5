package com.openclassrooms.mddapi.security;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import com.openclassrooms.mddapi.config.AuthProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final AuthProperties authProperties;
	private final JwtEncoder jwtEncoder;

	/**
	 * Creates a short-lived access token (JWT) for the given user id.
	 *
	 * @param userId the authenticated user identifier
	 * @return a signed JWT as a compact string
	 */
	public String createAccessToken(long userId) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(authProperties.accessTokenTtlSeconds());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(Long.toString(userId))
				.issuedAt(now)
				.expiresAt(expiresAt)
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
