package com.openclassrooms.mddapi.auth.dto;

public record LoginResponse(
		String accessToken,
		UserDto user
) {
}

