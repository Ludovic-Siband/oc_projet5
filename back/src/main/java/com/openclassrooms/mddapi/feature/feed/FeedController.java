package com.openclassrooms.mddapi.feature.feed;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.feature.feed.dto.FeedPostResponse;
import com.openclassrooms.mddapi.security.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

	private final FeedService feedService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public ResponseEntity<List<FeedPostResponse>> getFeed(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(name = "sort", defaultValue = "desc") FeedSort sort) {
		long userId = currentUserService.getUserId(jwt);
		return ResponseEntity.ok(feedService.getFeed(userId, sort));
	}
}
