package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.domain.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}

