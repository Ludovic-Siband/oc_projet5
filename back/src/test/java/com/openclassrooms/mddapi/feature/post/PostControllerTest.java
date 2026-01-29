package com.openclassrooms.mddapi.feature.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.TestWebConfig;
import com.openclassrooms.mddapi.feature.post.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostRequest;
import com.openclassrooms.mddapi.feature.post.dto.CreatePostResponse;
import com.openclassrooms.mddapi.feature.post.dto.PostDetailResponse;
import com.openclassrooms.mddapi.feature.post.dto.PostSubjectResponse;
import com.openclassrooms.mddapi.security.CurrentUserService;

@SpringBootTest(properties = {
		"spring.docker.compose.enabled=false",
		"app.auth.jwt-secret=0123456789abcdef0123456789abcdef",
		"app.auth.access-token-ttl-seconds=900",
		"app.auth.refresh-token-ttl-days=1",
		"app.auth.refresh-cookie-name=refreshToken",
		"app.auth.cookie-secure=false",
		"app.auth.cookie-same-site=Strict",
		"app.auth.cookie-path=/"
})
@Import(TestWebConfig.class)
class PostControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PostService postService;

	@MockitoBean
	private CurrentUserService currentUserService;

	@Test
	void createPostReturnsCreated() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		CreatePostRequest request = new CreatePostRequest(2L, "Title", "Content");
		when(postService.createPost(1L, request)).thenReturn(new CreatePostResponse(5L));

		mockMvc.perform(post("/api/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"subjectId\":2,\"title\":\"Title\",\"content\":\"Content\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(5L));
	}

	@Test
	void getPostReturnsDetails() throws Exception {
		PostDetailResponse response = new PostDetailResponse(5L, new PostSubjectResponse(2L, "Java"), "Title", "Content", "user", Instant.parse("2024-01-01T10:00:00Z"), Collections.emptyList());
		when(postService.getPost(5L)).thenReturn(response);

		mockMvc.perform(get("/api/posts/5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.subject.id").value(2L));
	}

	@Test
	void addCommentReturnsCreated() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		CreateCommentRequest request = new CreateCommentRequest("Hello");

		mockMvc.perform(post("/api/posts/5/comments")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"content\":\"Hello\"}"))
				.andExpect(status().isCreated());

		verify(postService).addComment(1L, 5L, request);
	}
}
