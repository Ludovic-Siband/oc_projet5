package com.openclassrooms.mddapi.feature.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		@Email
		String email,

		@Size(min = 3, max = 50)
		@Pattern(regexp = ".*\\S.*", message = "Le nom d'utilisateur est requis")
		String username,

		@Size(min = 8, max = 255)
		@Pattern(
				regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
				message = "Mot de passe invalide (min 8 caractères, au moins 1 minuscule, 1 majuscule, 1 chiffre et 1 caractère spécial)"
		)
		String password
) {
	@AssertTrue(message = "Au moins un champ doit être fourni")
	public boolean isAnyFieldProvided() {
		return email != null || username != null || password != null;
	}
}
