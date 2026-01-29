package com.openclassrooms.mddapi.feature.subject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.TestSupport;
import com.openclassrooms.mddapi.domain.Subject;
import com.openclassrooms.mddapi.domain.Subscription;
import com.openclassrooms.mddapi.domain.SubscriptionId;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.SubjectRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

	@Mock
	private SubjectRepository subjectRepository;

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private SubjectService subjectService;

	private User user;
	private Subject subject;

	@BeforeEach
	void setUp() {
		user = new User("user@mail.com", "user", "hashed");
		TestSupport.setId(user, 1L);
		subject = TestSupport.newInstance(Subject.class);
		subject.setName("Java");
		subject.setDescription("Lang");
		TestSupport.setId(subject, 10L);
	}

	@Test
	void listSubjectsMarksSubscribed() {
		Subject other = TestSupport.newInstance(Subject.class);
		other.setName("Spring");
		other.setDescription("Framework");
		TestSupport.setId(other, 11L);
		when(subscriptionRepository.findSubjectIdsByUserId(1L)).thenReturn(List.of(10L));
		when(subjectRepository.findAll()).thenReturn(List.of(subject, other));

		var response = subjectService.listSubjects(1L);

		assertThat(response).hasSize(2);
		assertThat(response.get(0).subscribed()).isTrue();
		assertThat(response.get(1).subscribed()).isFalse();
	}

	@Test
	void subscribeThrowsWhenUserMissing() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> subjectService.subscribe(1L, 10L))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void subscribeThrowsWhenSubjectMissing() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(subjectRepository.findById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> subjectService.subscribe(1L, 10L))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void subscribeSavesWhenNotSubscribed() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
		SubscriptionId id = new SubscriptionId(1L, 10L);
		when(subscriptionRepository.existsById(id)).thenReturn(false);

		var response = subjectService.subscribe(1L, 10L);

		assertThat(response.subscribed()).isTrue();
		verify(subscriptionRepository).save(any(Subscription.class));
	}

	@Test
	void subscribeNoopsWhenAlreadySubscribed() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
		SubscriptionId id = new SubscriptionId(1L, 10L);
		when(subscriptionRepository.existsById(id)).thenReturn(true);

		subjectService.subscribe(1L, 10L);

		verify(subscriptionRepository, never()).save(any(Subscription.class));
	}

	@Test
	void unsubscribeDeletesWhenSubscribed() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
		SubscriptionId id = new SubscriptionId(1L, 10L);
		when(subscriptionRepository.existsById(id)).thenReturn(true);

		var response = subjectService.unsubscribe(1L, 10L);

		assertThat(response.subscribed()).isFalse();
		verify(subscriptionRepository).deleteById(id);
	}

	@Test
	void unsubscribeNoopsWhenNotSubscribed() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
		SubscriptionId id = new SubscriptionId(1L, 10L);
		when(subscriptionRepository.existsById(id)).thenReturn(false);

		var response = subjectService.unsubscribe(1L, 10L);

		assertThat(response.subscribed()).isFalse();
		verify(subscriptionRepository, never()).deleteById(id);
	}
}
