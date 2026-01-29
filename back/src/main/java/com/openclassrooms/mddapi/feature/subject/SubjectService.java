package com.openclassrooms.mddapi.feature.subject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.domain.Subscription;
import com.openclassrooms.mddapi.domain.SubscriptionId;
import com.openclassrooms.mddapi.domain.Subject;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.feature.subject.dto.SubjectResponse;
import com.openclassrooms.mddapi.feature.subject.dto.SubscriptionStatusResponse;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.SubjectRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Manages subjects and user subscriptions.
 */
@Service
@RequiredArgsConstructor
public class SubjectService {

	private final SubjectRepository subjectRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;

	/**
	 * Lists all subjects with a subscription flag for the given user.
	 *
	 * @param userId the authenticated user id
	 * @return subjects with subscription status
	 */
	@Transactional(readOnly = true)
	public List<SubjectResponse> listSubjects(long userId) {
		List<Long> subscribedSubjectIds = subscriptionRepository.findSubjectIdsByUserId(userId);
		Set<Long> subscribedSubjectIdSet = new HashSet<>(subscribedSubjectIds);
		return subjectRepository.findAll().stream()
				.map(subject -> new SubjectResponse(
						subject.getId(),
						subject.getName(),
						subject.getDescription(),
						subscribedSubjectIdSet.contains(subject.getId())))
				.toList();
	}

	/**
	 * Subscribes the user to a subject if not already subscribed.
	 *
	 * @param userId    the authenticated user id
	 * @param subjectId the subject id
	 * @return subscription status response
	 * @throws NotFoundException if the user or subject does not exist
	 */
	@Transactional
	public SubscriptionStatusResponse subscribe(long userId, long subjectId) {
		User user = getUser(userId);
		Subject subject = subjectRepository.findById(subjectId)
				.orElseThrow(() -> new NotFoundException("Thème introuvable"));

		SubscriptionId id = new SubscriptionId(user.getId(), subject.getId());
		if (!subscriptionRepository.existsById(id)) {
			subscriptionRepository.save(new Subscription(user, subject));
		}

		return new SubscriptionStatusResponse(true);
	}

	/**
	 * Unsubscribes the user from a subject if currently subscribed.
	 *
	 * @param userId    the authenticated user id
	 * @param subjectId the subject id
	 * @return subscription status response
	 * @throws NotFoundException if the user or subject does not exist
	 */
	@Transactional
	public SubscriptionStatusResponse unsubscribe(long userId, long subjectId) {
		User user = getUser(userId);
		Subject subject = subjectRepository.findById(subjectId)
				.orElseThrow(() -> new NotFoundException("Thème introuvable"));

		SubscriptionId id = new SubscriptionId(user.getId(), subject.getId());
		if (subscriptionRepository.existsById(id)) {
			subscriptionRepository.deleteById(id);
		}

		return new SubscriptionStatusResponse(false);
	}

	/**
	 * Loads a user by id.
	 *
	 * @param userId the user id
	 * @return the user
	 * @throws NotFoundException if the user does not exist
	 */
	private User getUser(long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
	}
}
