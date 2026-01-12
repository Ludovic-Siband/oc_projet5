package com.openclassrooms.mddapi.feature.auth.dto;

public record RegisterResponse(
		Long id,
		String email,
		String username
) {
}

