package com.openclassrooms.mddapi.error;

public class ConflictException extends RuntimeException {
	public ConflictException(String message) {
		super(message);
	}
}

