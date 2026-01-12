package com.openclassrooms.mddapi.feature.post.dto;

import java.time.Instant;
import java.util.List;

public record PostDetailResponse(
		Long id,
		PostSubjectResponse subject,
		String title,
		String content,
		String author,
		Instant createdAt,
		List<CommentResponse> comments
) {
}
