package com.openclassrooms.mddapi.feature.auth;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.feature.auth.dto.LoginRequest;
import com.openclassrooms.mddapi.feature.auth.dto.LoginResponse;
import com.openclassrooms.mddapi.feature.auth.dto.RefreshResponse;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterRequest;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterResponse;
import com.openclassrooms.mddapi.feature.auth.dto.UserDto;
import com.openclassrooms.mddapi.domain.AuthSession;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.UnauthorizedException;
import com.openclassrooms.mddapi.repository.AuthSessionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Handles authentication workflows (register, login, refresh, logout).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final AuthSessionRepository authSessionRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

	/**
	 * Registers a new user.
	 * <p>
	 * Enforces unique email/username and stores a BCrypt-hashed password.
	 *
	 * @param request validated registration payload
	 * @return the created user (public fields only)
	 */
	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ConflictException("Cette adresse e-mail n'est pas disponible");
		}
		if (userRepository.existsByUsernameIgnoreCase(request.username())) {
			throw new ConflictException("Ce nom d'utilisateur n'est pas disponible");
		}

		User user = new User(request.email(), request.username(), passwordEncoder.encode(request.password()));

		User saved = userRepository.save(user);
		return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getUsername());
	}

	/**
	 * Authenticates a user and issues a new access token and refresh token.
	 * <p>
	 * The refresh token is returned so the controller can set it in an HttpOnly cookie; only a hash is stored in
	 * {@code auth_session}.
	 *
	 * @param request validated login payload
	 * @return access token + user data + refresh token (plaintext, for cookie only)
	 */
	@Transactional
	public LoginResult login(LoginRequest request) {
		User user = findByIdentifier(request.identifier());

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new UnauthorizedException("Nom d'utilisateur ou mot de passe incorrect");
		}

		String refreshToken = refreshTokenService.generateToken();
		String refreshTokenHash = refreshTokenService.hashToken(refreshToken);

		AuthSession session = new AuthSession(user, refreshTokenHash, refreshTokenService.computeExpiresAt());
		authSessionRepository.save(session);

		String accessToken = jwtService.createAccessToken(user.getId());
		return new LoginResult(accessToken, toDto(user), refreshToken);
	}

	/**
	 * Rotates the refresh token and returns a new access token.
	 * <p>
	 * The provided refresh token must be present, valid, unrevoked, and unexpired.
	 *
	 * @param refreshToken refresh token in plaintext (from HttpOnly cookie)
	 * @return new access token + new refresh token (plaintext, for cookie only)
	 */
	@Transactional
	public RefreshResult refresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new UnauthorizedException("Le refresh token est manquant");
		}

		String tokenHash = refreshTokenService.hashToken(refreshToken);
		AuthSession session = authSessionRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new UnauthorizedException("Le refresh token est invalide"));

		if (session.getRevokedAt() != null) {
			throw new UnauthorizedException("La session a été révoquée");
		}
		if (session.getExpiresAt().isBefore(Instant.now())) {
			throw new UnauthorizedException("La session est expirée");
		}

		String newRefreshToken = refreshTokenService.generateToken();
		session.setTokenHash(refreshTokenService.hashToken(newRefreshToken));
		session.setExpiresAt(refreshTokenService.computeExpiresAt());
		authSessionRepository.save(session);

		String accessToken = jwtService.createAccessToken(session.getUser().getId());
		return new RefreshResult(new RefreshResponse(accessToken), newRefreshToken);
	}

	/**
	 * Revokes the server-side session associated with the provided refresh token.
	 * <p>
	 * This operation is idempotent and succeeds even if the token is missing or unknown.
	 *
	 * @param refreshToken refresh token in plaintext (from HttpOnly cookie), may be null/blank
	 */
	@Transactional
	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return;
		}
		String tokenHash = refreshTokenService.hashToken(refreshToken);
		authSessionRepository.findByTokenHash(tokenHash).ifPresent(session -> {
			if (session.getRevokedAt() == null) {
				session.setRevokedAt(Instant.now());
				authSessionRepository.save(session);
			}
		});
	}

	private User findByIdentifier(String identifier) {
		return (identifier.contains("@")
				? userRepository.findByEmailIgnoreCase(identifier)
				: userRepository.findByUsernameIgnoreCase(identifier))
				.orElseThrow(() -> new UnauthorizedException("Nom d'utilisateur ou mot de passe incorrect"));
	}

	private static UserDto toDto(User user) {
		return new UserDto(user.getId(), user.getEmail(), user.getUsername());
	}

	public record LoginResult(String accessToken, UserDto user, String refreshToken) {
		public LoginResponse toResponse() {
			return new LoginResponse(accessToken, user);
		}
	}

	public record RefreshResult(RefreshResponse response, String refreshToken) {
	}
}
