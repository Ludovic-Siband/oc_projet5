package com.openclassrooms.mddapi.feature.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.domain.AuthSession;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.UnauthorizedException;
import com.openclassrooms.mddapi.feature.auth.dto.LoginRequest;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterRequest;
import com.openclassrooms.mddapi.repository.AuthSessionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import com.openclassrooms.mddapi.TestSupport;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthSessionRepository authSessionRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@InjectMocks
	private AuthService authService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User("user@mail.com", "user", "hashed");
		TestSupport.setId(user, 42L);
	}

	@Test
	void registerThrowsWhenEmailTaken() {
		RegisterRequest request = new RegisterRequest("user@mail.com", "user", "Password1!");
		when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("adresse e-mail");
	}

	@Test
	void registerThrowsWhenUsernameTaken() {
		RegisterRequest request = new RegisterRequest("new@mail.com", "user", "Password1!");
		when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
		when(userRepository.existsByUsernameIgnoreCase(request.username())).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("nom d'utilisateur");
	}

	@Test
	void registerCreatesUser() {
		RegisterRequest request = new RegisterRequest("new@mail.com", "newuser", "Password1!");
		when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
		when(userRepository.existsByUsernameIgnoreCase(request.username())).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded");
		User saved = new User(request.email(), request.username(), "encoded");
		TestSupport.setId(saved, 100L);
		when(userRepository.save(any(User.class))).thenReturn(saved);

		var response = authService.register(request);

		assertThat(response.id()).isEqualTo(100L);
		assertThat(response.email()).isEqualTo(request.email());
		assertThat(response.username()).isEqualTo(request.username());
	}

	@Test
	void loginWithEmailSucceeds() {
		LoginRequest request = new LoginRequest("user@mail.com", "Password1!");
		when(userRepository.findByEmailIgnoreCase(request.identifier())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
		when(refreshTokenService.generateToken()).thenReturn("refresh");
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		Instant expiresAt = Instant.now().plusSeconds(3600);
		when(refreshTokenService.computeExpiresAt()).thenReturn(expiresAt);
		when(jwtService.createAccessToken(user.getId())).thenReturn("access");

		AuthService.LoginResult result = authService.login(request);

		assertThat(result.accessToken()).isEqualTo("access");
		assertThat(result.refreshToken()).isEqualTo("refresh");
		assertThat(result.user().id()).isEqualTo(user.getId());

		ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
		verify(authSessionRepository).save(captor.capture());
		assertThat(captor.getValue().getTokenHash()).isEqualTo("hash");
		assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiresAt);
	}

	@Test
	void loginWithUsernameSucceeds() {
		LoginRequest request = new LoginRequest("user", "Password1!");
		when(userRepository.findByUsernameIgnoreCase(request.identifier())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
		when(refreshTokenService.generateToken()).thenReturn("refresh");
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(refreshTokenService.computeExpiresAt()).thenReturn(Instant.now().plusSeconds(3600));
		when(jwtService.createAccessToken(user.getId())).thenReturn("access");

		AuthService.LoginResult result = authService.login(request);

		assertThat(result.user().username()).isEqualTo("user");
	}

	@Test
	void loginThrowsOnBadPassword() {
		LoginRequest request = new LoginRequest("user@mail.com", "bad");
		when(userRepository.findByEmailIgnoreCase(request.identifier())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("mot de passe");
	}

	@Test
	void loginThrowsWhenUserMissing() {
		LoginRequest request = new LoginRequest("user@mail.com", "Password1!");
		when(userRepository.findByEmailIgnoreCase(request.identifier())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void refreshRejectsMissingToken() {
		assertThatThrownBy(() -> authService.refresh(""))
				.isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("manquant");
	}

	@Test
	void refreshRejectsInvalidToken() {
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(authSessionRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.refresh("refresh"))
				.isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("invalide");
	}

	@Test
	void refreshRejectsRevokedSession() {
		AuthSession session = new AuthSession(user, "hash", Instant.now().plusSeconds(3600));
		session.setRevokedAt(Instant.now());
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(authSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> authService.refresh("refresh"))
				.isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("révoquée");
	}

	@Test
	void refreshRejectsExpiredSession() {
		AuthSession session = new AuthSession(user, "hash", Instant.now().minusSeconds(5));
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(authSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> authService.refresh("refresh"))
				.isInstanceOf(UnauthorizedException.class)
				.hasMessageContaining("expirée");
	}

	@Test
	void refreshRotatesToken() {
		AuthSession session = new AuthSession(user, "hash", Instant.now().plusSeconds(3600));
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(authSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));
		when(refreshTokenService.generateToken()).thenReturn("newRefresh");
		when(refreshTokenService.hashToken("newRefresh")).thenReturn("newHash");
		Instant newExpiresAt = Instant.now().plusSeconds(7200);
		when(refreshTokenService.computeExpiresAt()).thenReturn(newExpiresAt);
		when(jwtService.createAccessToken(user.getId())).thenReturn("access");

		AuthService.RefreshResult result = authService.refresh("refresh");

		assertThat(result.refreshToken()).isEqualTo("newRefresh");
		assertThat(result.response().accessToken()).isEqualTo("access");
		assertThat(session.getTokenHash()).isEqualTo("newHash");
		assertThat(session.getExpiresAt()).isEqualTo(newExpiresAt);
		verify(authSessionRepository).save(session);
	}

	@Test
	void logoutNoopsOnBlankToken() {
		authService.logout(" ");
		verify(authSessionRepository, never()).findByTokenHash(any());
	}

	@Test
	void logoutRevokesSession() {
		AuthSession session = new AuthSession(user, "hash", Instant.now().plusSeconds(3600));
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(authSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));

		authService.logout("refresh");

		assertThat(session.getRevokedAt()).isNotNull();
		verify(authSessionRepository).save(session);
	}

	@Test
	void logoutNoopsWhenAlreadyRevoked() {
		AuthSession session = new AuthSession(user, "hash", Instant.now().plusSeconds(3600));
		session.setRevokedAt(Instant.now());
		when(refreshTokenService.hashToken("refresh")).thenReturn("hash");
		when(authSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));

		authService.logout("refresh");

		verify(authSessionRepository, never()).save(session);
	}
}
