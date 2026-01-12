package com.openclassrooms.mddapi.exception;

import java.util.Map;

public record ApiError(
		String error,
		String message,
		Map<String, String> fields
) {
	public static ApiError simple(String error, String message) {
		return new ApiError(error, message, null);
	}
}

