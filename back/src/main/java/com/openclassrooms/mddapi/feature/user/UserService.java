package com.openclassrooms.mddapi.feature.user;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.feature.auth.dto.UserDto;
import com.openclassrooms.mddapi.domain.Subscription;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.ConflictException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.feature.user.dto.SubscriptionDto;
import com.openclassrooms.mddapi.feature.user.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.feature.user.dto.UserProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public UserProfileResponse getProfile(long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

		List<Subscription> subscriptions = subscriptionRepository.findAllByUserIdWithSubject(userId);
		List<SubscriptionDto> subscriptionDtos = subscriptions.stream()
				.map(subscription -> new SubscriptionDto(
						subscription.getSubject().getId(),
						subscription.getSubject().getName(),
						subscription.getSubject().getDescription()))
				.toList();

		return new UserProfileResponse(user.getId(), user.getEmail(), user.getUsername(), subscriptionDtos);
	}

	@Transactional
	public UserDto updateProfile(long userId, UpdateUserRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

		if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
			if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), userId)) {
				throw new ConflictException("Cette adresse e-mail n'est pas disponible");
			}
			user.setEmail(request.email());
		}

		if (request.username() != null && !request.username().equalsIgnoreCase(user.getUsername())) {
			if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), userId)) {
				throw new ConflictException("Ce nom d'utilisateur n'est pas disponible");
			}
			user.setUsername(request.username());
		}

		if (request.password() != null) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}

		User saved = userRepository.save(user);
		return new UserDto(saved.getId(), saved.getEmail(), saved.getUsername());
	}
}
