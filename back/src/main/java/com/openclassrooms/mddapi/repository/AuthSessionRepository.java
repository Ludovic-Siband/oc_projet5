package com.openclassrooms.mddapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.domain.AuthSession;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
	Optional<AuthSession> findByTokenHash(String tokenHash);
}
