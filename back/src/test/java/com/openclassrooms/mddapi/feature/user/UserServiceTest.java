package com.openclassrooms.mddapi.feature.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.TestSupport;
import com.openclassrooms.mddapi.domain.Subscription;
import com.openclassrooms.mddapi.domain.Subject;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.feature.user.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User("user@mail.com", "user", "hashed");
		TestSupport.setId(user, 1L);
	}

	@Test
	void getProfileThrowsWhenUserMissing() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getProfile(1L))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getProfileMapsSubscriptions() {
		Subject subject = TestSupport.newInstance(Subject.class);
		subject.setName("Java");
		subject.setDescription("Lang");
		TestSupport.setId(subject, 10L);
		Subscription subscription = new Subscription(user, subject);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(subscriptionRepository.findAllByUserIdWithSubject(1L)).thenReturn(List.of(subscription));

		var response = userService.getProfile(1L);

		assertThat(response.subscriptions()).hasSize(1);
		assertThat(response.subscriptions().get(0).subjectId()).isEqualTo(10L);
	}

	@Test
	void updateProfileThrowsOnEmailConflict() {
		UpdateUserRequest request = new UpdateUserRequest("new@mail.com", null, null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1L)).thenReturn(true);

		assertThatThrownBy(() -> userService.updateProfile(1L, request))
				.isInstanceOf(ConflictException.class);
	}

	@Test
	void updateProfileThrowsOnUsernameConflict() {
		UpdateUserRequest request = new UpdateUserRequest(null, "newuser", null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), 1L)).thenReturn(true);

		assertThatThrownBy(() -> userService.updateProfile(1L, request))
				.isInstanceOf(ConflictException.class);
	}

	@Test
	void updateProfileUpdatesPasswordAndSaves() {
		UpdateUserRequest request = new UpdateUserRequest("new@mail.com", "newuser", "Password1!");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1L)).thenReturn(false);
		when(userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), 1L)).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = userService.updateProfile(1L, request);

		assertThat(response.email()).isEqualTo("new@mail.com");
		assertThat(response.username()).isEqualTo("newuser");
		assertThat(user.getPassword()).isEqualTo("encoded");
		verify(userRepository).save(user);
	}
}
