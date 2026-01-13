package com.openclassrooms.mddapi.feature.post;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.feature.post.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostResponse;
import com.openclassrooms.mddapi.feature.post.dto.PostDetailResponse;
import com.openclassrooms.mddapi.security.CurrentUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final CurrentUserService currentUserService;

	@PostMapping
	public ResponseEntity<CreatePostResponse> createPost(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreatePostRequest request) {
		long userId = currentUserService.getUserId(jwt);
		CreatePostResponse response = postService.createPost(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostDetailResponse> getPost(@PathVariable("id") long postId) {
		return ResponseEntity.ok(postService.getPost(postId));
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<Void> addComment(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable("id") long postId,
			@Valid @RequestBody CreateCommentRequest request) {
		long userId = currentUserService.getUserId(jwt);
		postService.addComment(userId, postId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
