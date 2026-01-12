package com.openclassrooms.mddapi.feature.subject;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.feature.subject.dto.SubjectResponse;
import com.openclassrooms.mddapi.feature.subject.dto.SubscriptionStatusResponse;
import com.openclassrooms.mddapi.security.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

	private final SubjectService subjectService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public ResponseEntity<List<SubjectResponse>> list(@AuthenticationPrincipal Jwt jwt) {
		long userId = currentUserService.getUserId(jwt);
		return ResponseEntity.ok(subjectService.listSubjects(userId));
	}

	@PostMapping("/{id}/subscribe")
	public ResponseEntity<SubscriptionStatusResponse> subscribe(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable("id") long subjectId) {
		long userId = currentUserService.getUserId(jwt);
		return ResponseEntity.ok(subjectService.subscribe(userId, subjectId));
	}

	@DeleteMapping("/{id}/subscribe")
	public ResponseEntity<SubscriptionStatusResponse> unsubscribe(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable("id") long subjectId) {
		long userId = currentUserService.getUserId(jwt);
		return ResponseEntity.ok(subjectService.unsubscribe(userId, subjectId));
	}
}
