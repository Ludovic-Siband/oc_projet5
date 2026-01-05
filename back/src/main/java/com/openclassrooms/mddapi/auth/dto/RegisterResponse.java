package com.openclassrooms.mddapi.auth.dto;

public record RegisterResponse(
		Long id,
		String email,
		String username
) {
}

