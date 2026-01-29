package com.openclassrooms.mddapi.feature.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.TestSupport;
import com.openclassrooms.mddapi.domain.Post;
import com.openclassrooms.mddapi.domain.Subject;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Mock
	private PostRepository postRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private FeedService feedService;

	private User author;
	private Subject subject;

	@BeforeEach
	void setUp() {
		author = new User("user@mail.com", "user", "hashed");
		TestSupport.setId(author, 1L);
		subject = TestSupport.newInstance(Subject.class);
		subject.setName("Java");
		subject.setDescription("Lang");
		TestSupport.setId(subject, 2L);
	}

	@Test
	void getFeedThrowsWhenUserMissing() {
		when(userRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> feedService.getFeed(1L, FeedSort.desc))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getFeedReturnsEmptyWhenNoSubscriptions() {
		when(userRepository.existsById(1L)).thenReturn(true);
		when(subscriptionRepository.findSubjectIdsByUserId(1L)).thenReturn(Collections.emptyList());

		var response = feedService.getFeed(1L, FeedSort.desc);

		assertThat(response).isEmpty();
	}

	@Test
	void getFeedUsesAscendingSort() {
		when(userRepository.existsById(1L)).thenReturn(true);
		when(subscriptionRepository.findSubjectIdsByUserId(1L)).thenReturn(List.of(2L));
		Post post = new Post(subject, author, "Title", "Content");
		TestSupport.setId(post, 5L);
		TestSupport.setCreatedAt(post, Instant.parse("2024-01-01T10:00:00Z"));
		when(postRepository.findFeedPostsAsc(List.of(2L))).thenReturn(List.of(post));

		var response = feedService.getFeed(1L, FeedSort.asc);

		assertThat(response).hasSize(1);
		verify(postRepository).findFeedPostsAsc(List.of(2L));
	}

	@Test
	void getFeedUsesDescendingSort() {
		when(userRepository.existsById(1L)).thenReturn(true);
		when(subscriptionRepository.findSubjectIdsByUserId(1L)).thenReturn(List.of(2L));
		Post post = new Post(subject, author, "Title", "Content");
		TestSupport.setId(post, 5L);
		TestSupport.setCreatedAt(post, Instant.parse("2024-01-01T10:00:00Z"));
		when(postRepository.findFeedPostsDesc(List.of(2L))).thenReturn(List.of(post));

		feedService.getFeed(1L, FeedSort.desc);

		verify(postRepository).findFeedPostsDesc(List.of(2L));
	}
}
