package com.openclassrooms.mddapi.feature.post.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
		@NotBlank String content
) {
}
