package com.openclassrooms.mddapi.feature.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.TestWebConfig;
import com.openclassrooms.mddapi.feature.auth.dto.UserDto;
import com.openclassrooms.mddapi.feature.user.dto.SubscriptionDto;
import com.openclassrooms.mddapi.feature.user.dto.UpdateUserRequest;
import com.openclassrooms.mddapi.feature.user.dto.UserProfileResponse;
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
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private CurrentUserService currentUserService;

	@Test
	void meReturnsProfile() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		UserProfileResponse profile = new UserProfileResponse(1L, "user@mail.com", "user", List.of(new SubscriptionDto(10L, "Java", "Lang")));
		when(userService.getProfile(1L)).thenReturn(profile);

		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.subscriptions").isArray());
	}

	@Test
	void updateMeReturnsUserDto() throws Exception {
		when(currentUserService.getUserId(any())).thenReturn(1L);
		UpdateUserRequest request = new UpdateUserRequest("new@mail.com", null, null);
		when(userService.updateProfile(1L, request)).thenReturn(new UserDto(1L, "new@mail.com", "user"));

		mockMvc.perform(put("/api/users/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"new@mail.com\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("new@mail.com"));

		verify(userService).updateProfile(1L, request);
	}
}
