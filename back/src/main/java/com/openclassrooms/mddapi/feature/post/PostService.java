package com.openclassrooms.mddapi.feature.post;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.domain.Comment;
import com.openclassrooms.mddapi.domain.Post;
import com.openclassrooms.mddapi.domain.Subject;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.feature.post.dto.CommentResponse;
import com.openclassrooms.mddapi.feature.post.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostResponse;
import com.openclassrooms.mddapi.feature.post.dto.PostDetailResponse;
import com.openclassrooms.mddapi.feature.post.dto.PostSubjectResponse;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubjectRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Handles post creation and comment management.
 */
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final SubjectRepository subjectRepository;
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;

	/**
	 * Creates a new post for the given subject and author: both must exist.
	 *
	 * @param userId  the authenticated user id
	 * @param request post creation payload
	 * @return the created post id
	 * @throws NotFoundException if the subject or user does not exist
	 */
	@Transactional
	public CreatePostResponse createPost(long userId, CreatePostRequest request) {
		Subject subject = subjectRepository.findById(request.subjectId())
				.orElseThrow(() -> new NotFoundException("Thème introuvable"));
		User author = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

		Post saved = postRepository.save(new Post(subject, author, request.title(), request.content()));
		return new CreatePostResponse(saved.getId());
	}

	/**
	 * Loads a post with its author, subject, and comments.
	 *
	 * @param postId the post id
	 * @return post details including comments
	 * @throws NotFoundException if the post does not exist
	 */
	@Transactional(readOnly = true)
	public PostDetailResponse getPost(long postId) {
		Post post = postRepository.findByIdWithAuthorAndSubject(postId)
				.orElseThrow(() -> new NotFoundException("Article introuvable"));

		List<CommentResponse> comments = commentRepository.findByPostIdWithAuthor(postId).stream()
				.map(comment -> new CommentResponse(
						comment.getId(),
						comment.getContent(),
						comment.getAuthor().getUsername(),
						comment.getCreatedAt()))
				.toList();

		return new PostDetailResponse(
				post.getId(),
				new PostSubjectResponse(post.getSubject().getId(), post.getSubject().getName()),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getUsername(),
				post.getCreatedAt(),
				comments);
	}

	/**
	 * Adds a comment to a post by a given user.
	 *
	 * @param userId  the authenticated user id
	 * @param postId  the post id
	 * @param request comment payload
	 * @throws NotFoundException if the post or user does not exist
	 */
	@Transactional
	public void addComment(long userId, long postId, CreateCommentRequest request) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new NotFoundException("Article introuvable"));
		User author = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

		commentRepository.save(new Comment(post, author, request.content()));
	}
}
