package com.openclassrooms.mddapi.feature.auth;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.feature.auth.dto.LoginRequest;
import com.openclassrooms.mddapi.feature.auth.dto.LoginResponse;
import com.openclassrooms.mddapi.feature.auth.dto.MessageResponse;
import com.openclassrooms.mddapi.feature.auth.dto.RefreshResponse;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterRequest;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterResponse;
import com.openclassrooms.mddapi.config.AuthProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final RefreshCookieService refreshCookieService;
	private final AuthProperties authProperties;

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
		RegisterResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthService.LoginResult result = authService.login(request);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()).toString())
				.body(result.toResponse());
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refresh(HttpServletRequest request) {
		String refreshToken = getCookieValue(request, authProperties.refreshCookieName());
		AuthService.RefreshResult result = authService.refresh(refreshToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookieService.buildRefreshCookie(result.refreshToken()).toString())
				.body(result.response());
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
		String refreshToken = getCookieValue(request, authProperties.refreshCookieName());
		authService.logout(refreshToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookieService.clearRefreshCookie().toString())
				.body(new MessageResponse("Vous avez été déconnecté avec succès"));
	}

	private static String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			return null;
		}
		return Arrays.stream(cookies)
				.filter(c -> name.equals(c.getName()))
				.findFirst()
				.map(Cookie::getValue)
				.orElse(null);
	}
}

