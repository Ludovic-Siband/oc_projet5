package com.openclassrooms.mddapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.openclassrooms.mddapi.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("""
			select p from Post p
			join fetch p.author
			join fetch p.subject
			where p.subject.id in :subjectIds
			order by p.createdAt asc
			""")
	List<Post> findFeedPostsAsc(@Param("subjectIds") List<Long> subjectIds);

	@Query("""
			select p from Post p
			join fetch p.author
			join fetch p.subject
			where p.subject.id in :subjectIds
			order by p.createdAt desc
			""")
	List<Post> findFeedPostsDesc(@Param("subjectIds") List<Long> subjectIds);

	@Query("""
			select p from Post p
			join fetch p.author
			join fetch p.subject
			where p.id = :postId
			""")
	Optional<Post> findByIdWithAuthorAndSubject(@Param("postId") Long postId);
}
