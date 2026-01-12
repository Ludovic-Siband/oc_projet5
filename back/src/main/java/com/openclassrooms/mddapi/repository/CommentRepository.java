package com.openclassrooms.mddapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.openclassrooms.mddapi.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("""
			select c from Comment c
			join fetch c.author
			where c.post.id = :postId
			order by c.createdAt asc
			""")
	List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);
}
