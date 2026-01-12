package com.openclassrooms.mddapi.feature.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.feature.auth.dto.UserDto;
import com.openclassrooms.mddapi.feature.user.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.feature.user.dto.UserProfileResponse;
import com.openclassrooms.mddapi.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final CurrentUserService currentUserService;

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
		long userId = currentUserService.getUserId(jwt);
		return ResponseEntity.ok(userService.getProfile(userId));
	}

	@PutMapping("/me")
	public ResponseEntity<UserDto> updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateUserRequest request) {
		long userId = currentUserService.getUserId(jwt);
		return ResponseEntity.ok(userService.updateProfile(userId, request));
	}
}
