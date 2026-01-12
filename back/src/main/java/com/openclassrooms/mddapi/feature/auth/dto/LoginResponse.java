package com.openclassrooms.mddapi.feature.auth.dto;

public record LoginResponse(
		String accessToken,
		UserDto user
) {
}

