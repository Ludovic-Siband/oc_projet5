package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.domain.AuthSession;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
}

