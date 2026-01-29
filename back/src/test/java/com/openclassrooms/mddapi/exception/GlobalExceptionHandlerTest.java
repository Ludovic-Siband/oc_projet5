package com.openclassrooms.mddapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void handleUnauthorizedReturns401() {
		var response = handler.handleUnauthorized(new UnauthorizedException("nope"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody().error()).isEqualTo("UNAUTHORIZED");
	}

	@Test
	void handleConflictReturns409() {
		var response = handler.handleConflict(new ConflictException("conflict"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().error()).isEqualTo("CONFLICT");
	}

	@Test
	void handleNotFoundReturns404() {
		var response = handler.handleNotFound(new NotFoundException("missing"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().error()).isEqualTo("NOT_FOUND");
	}

	@Test
	void handleIntegrityReturns409() {
		var response = handler.handleIntegrity(new DataIntegrityViolationException("duplicate"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().message()).contains("Ressource déjà existante");
	}
}
