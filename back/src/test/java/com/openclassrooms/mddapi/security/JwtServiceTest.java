package com.openclassrooms.mddapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.openclassrooms.mddapi.config.AuthProperties;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock
	private JwtEncoder jwtEncoder;

	@Test
	void createAccessTokenUsesSubjectAndTtl() {
		AuthProperties props = new AuthProperties("0123456789abcdef0123456789abcdef", 600, 1, "refresh", false, "Strict", "/");
		JwtService service = new JwtService(props, jwtEncoder);
		Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("1").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600)).build();
		when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(jwt);

		String token = service.createAccessToken(123L);

		assertThat(token).isEqualTo("token");
		ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
		verify(jwtEncoder).encode(captor.capture());
		assertThat(captor.getValue().getClaims().getSubject()).isEqualTo("123");
		assertThat(captor.getValue().getClaims().getExpiresAt()).isAfter(captor.getValue().getClaims().getIssuedAt());
	}
}
