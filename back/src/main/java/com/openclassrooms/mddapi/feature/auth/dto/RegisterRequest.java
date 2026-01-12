package com.openclassrooms.mddapi.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		
	@NotBlank
	@Email 
	String email,
		
	@NotBlank
	@Size(min = 3, max = 50)
	String username,

	@NotBlank
	@Size(min = 8, max = 255)
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
			message = "Mot de passe invalide (min 8 caractères, au moins 1 minuscule, 1 majuscule, 1 chiffre et 1 caractère spécial)"
	)
	String password
) {
}
