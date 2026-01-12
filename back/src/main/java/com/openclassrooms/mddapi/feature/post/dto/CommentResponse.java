package com.openclassrooms.mddapi.feature.post.dto;

import java.time.Instant;

public record CommentResponse(Long id, String content, String author, Instant createdAt) {
}
