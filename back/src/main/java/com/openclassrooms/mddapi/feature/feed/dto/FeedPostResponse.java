package com.openclassrooms.mddapi.feature.feed.dto;

import java.time.Instant;

public record FeedPostResponse(Long id, Long subjectId, String author, String title, String content, Instant createdAt) {
}
