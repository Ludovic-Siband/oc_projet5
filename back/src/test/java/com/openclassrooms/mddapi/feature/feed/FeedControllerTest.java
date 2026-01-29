package com.openclassrooms.mddapi.feature.feed;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.TestWebConfig;
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
class FeedControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FeedService feedService;

	@MockitoBean
	private CurrentUserService currentUserService;

	@Test
	void getFeedDefaultsToDesc() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		when(feedService.getFeed(1L, FeedSort.desc)).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/api/feed"))
				.andExpect(status().isOk());

		verify(feedService).getFeed(1L, FeedSort.desc);
	}

	@Test
	void getFeedUsesAscSort() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		when(feedService.getFeed(1L, FeedSort.asc)).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/api/feed?sort=asc"))
				.andExpect(status().isOk());

		verify(feedService).getFeed(1L, FeedSort.asc);
	}
}
