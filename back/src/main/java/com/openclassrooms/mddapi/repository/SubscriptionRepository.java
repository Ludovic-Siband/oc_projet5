package com.openclassrooms.mddapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.openclassrooms.mddapi.domain.Subscription;
import com.openclassrooms.mddapi.domain.SubscriptionId;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {

	@Query("select s from Subscription s join fetch s.subject where s.user.id = :userId")
	List<Subscription> findAllByUserIdWithSubject(@Param("userId") Long userId);
}
