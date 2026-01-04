package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.domain.Subscription;
import com.openclassrooms.mddapi.domain.SubscriptionId;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
}

