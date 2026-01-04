package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}

