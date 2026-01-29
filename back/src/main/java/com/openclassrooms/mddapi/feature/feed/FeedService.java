package com.openclassrooms.mddapi.feature.feed;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.domain.Post;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.feature.feed.dto.FeedPostResponse;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Handles feed retrieval and sorting for subscribed subjects.
 */
@Service
@RequiredArgsConstructor
public class FeedService {

	private final SubscriptionRepository subscriptionRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	/**
	 * Returns the feed for a user, filtered by their subject subscriptions and sorted by creation date.
	 *
	 * @param userId the authenticated user id
	 * @param sort   sort order (ascending or descending)
	 * @return a list of feed items
	 * @throws NotFoundException if the user does not exist
	 */
	@Transactional(readOnly = true)
	public List<FeedPostResponse> getFeed(long userId, FeedSort sort) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("Utilisateur introuvable");
		}

		List<Long> subjectIds = subscriptionRepository.findSubjectIdsByUserId(userId);
		if (subjectIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Post> posts = sort == FeedSort.asc
				? postRepository.findFeedPostsAsc(subjectIds)
				: postRepository.findFeedPostsDesc(subjectIds);

		return posts.stream()
				.map(post -> new FeedPostResponse(
						post.getId(),
						post.getSubject().getId(),
						post.getAuthor().getUsername(),
						post.getTitle(),
						post.getContent(),
						post.getCreatedAt()))
				.toList();
	}
}
