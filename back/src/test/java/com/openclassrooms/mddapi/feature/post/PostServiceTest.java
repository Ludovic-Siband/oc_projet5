package com.openclassrooms.mddapi.feature.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.TestSupport;
import com.openclassrooms.mddapi.domain.Comment;
import com.openclassrooms.mddapi.domain.Post;
import com.openclassrooms.mddapi.domain.Subject;
import com.openclassrooms.mddapi.domain.User;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.feature.post.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostRequest;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubjectRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@Mock
	private PostRepository postRepository;

	@Mock
	private SubjectRepository subjectRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private PostService postService;

	private User author;
	private Subject subject;

	@BeforeEach
	void setUp() {
		author = new User("user@mail.com", "user", "hashed");
		TestSupport.setId(author, 1L);
		subject = TestSupport.newInstance(Subject.class);
		subject.setName("Java");
		subject.setDescription("Lang");
		TestSupport.setId(subject, 2L);
	}

	@Test
	void createPostThrowsWhenSubjectMissing() {
		CreatePostRequest request = new CreatePostRequest(2L, "Title", "Content");
		when(subjectRepository.findById(2L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.createPost(1L, request))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void createPostThrowsWhenUserMissing() {
		CreatePostRequest request = new CreatePostRequest(2L, "Title", "Content");
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.createPost(1L, request))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void createPostReturnsId() {
		CreatePostRequest request = new CreatePostRequest(2L, "Title", "Content");
		when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
		when(userRepository.findById(1L)).thenReturn(Optional.of(author));
		Post saved = new Post(subject, author, request.title(), request.content());
		TestSupport.setId(saved, 50L);
		when(postRepository.save(any(Post.class))).thenReturn(saved);

		var response = postService.createPost(1L, request);

		assertThat(response.id()).isEqualTo(50L);
	}

	@Test
	void getPostThrowsWhenMissing() {
		when(postRepository.findByIdWithAuthorAndSubject(5L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.getPost(5L))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void getPostMapsDetailsAndComments() {
		Post post = new Post(subject, author, "Title", "Content");
		TestSupport.setId(post, 5L);
		TestSupport.setCreatedAt(post, Instant.parse("2024-01-01T10:00:00Z"));
		when(postRepository.findByIdWithAuthorAndSubject(5L)).thenReturn(Optional.of(post));
		Comment comment = new Comment(post, author, "Nice");
		TestSupport.setId(comment, 7L);
		TestSupport.setCreatedAt(comment, Instant.parse("2024-01-01T11:00:00Z"));
		when(commentRepository.findByPostIdWithAuthor(5L)).thenReturn(List.of(comment));

		var response = postService.getPost(5L);

		assertThat(response.id()).isEqualTo(5L);
		assertThat(response.subject().id()).isEqualTo(2L);
		assertThat(response.comments()).hasSize(1);
		assertThat(response.comments().get(0).content()).isEqualTo("Nice");
	}

	@Test
	void addCommentThrowsWhenPostMissing() {
		CreateCommentRequest request = new CreateCommentRequest("Hello");
		when(postRepository.findById(5L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.addComment(1L, 5L, request))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void addCommentThrowsWhenUserMissing() {
		CreateCommentRequest request = new CreateCommentRequest("Hello");
		Post post = new Post(subject, author, "Title", "Content");
		when(postRepository.findById(5L)).thenReturn(Optional.of(post));
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.addComment(1L, 5L, request))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void addCommentSavesComment() {
		CreateCommentRequest request = new CreateCommentRequest("Hello");
		Post post = new Post(subject, author, "Title", "Content");
		when(postRepository.findById(5L)).thenReturn(Optional.of(post));
		when(userRepository.findById(1L)).thenReturn(Optional.of(author));

		postService.addComment(1L, 5L, request);

		ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
		verify(commentRepository).save(captor.capture());
		assertThat(captor.getValue().getContent()).isEqualTo("Hello");
		assertThat(captor.getValue().getAuthor()).isEqualTo(author);
		assertThat(captor.getValue().getPost()).isEqualTo(post);
	}
}
