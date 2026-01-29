package com.openclassrooms.mddapi.feature.subject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.TestWebConfig;
import com.openclassrooms.mddapi.feature.subject.dto.SubjectResponse;
import com.openclassrooms.mddapi.feature.subject.dto.SubscriptionStatusResponse;
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
class SubjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SubjectService subjectService;

	@MockitoBean
	private CurrentUserService currentUserService;

	@Test
	void listReturnsSubjects() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		when(subjectService.listSubjects(1L)).thenReturn(List.of(new SubjectResponse(10L, "Java", "Lang", true)));

		mockMvc.perform(get("/api/subjects"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(10L))
				.andExpect(jsonPath("$[0].subscribed").value(true));
	}

	@Test
	void subscribeReturnsStatus() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		when(subjectService.subscribe(1L, 10L)).thenReturn(new SubscriptionStatusResponse(true));

		mockMvc.perform(post("/api/subjects/10/subscribe"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subscribed").value(true));

		verify(subjectService).subscribe(1L, 10L);
	}

	@Test
	void unsubscribeReturnsStatus() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		when(subjectService.unsubscribe(1L, 10L)).thenReturn(new SubscriptionStatusResponse(false));

		mockMvc.perform(delete("/api/subjects/10/subscribe"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subscribed").value(false));

		verify(subjectService).unsubscribe(1L, 10L);
	}
}
