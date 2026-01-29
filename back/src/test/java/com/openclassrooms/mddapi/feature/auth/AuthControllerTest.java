package com.openclassrooms.mddapi.feature.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.TestWebConfig;
import com.openclassrooms.mddapi.feature.auth.dto.LoginRequest;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterRequest;
import com.openclassrooms.mddapi.feature.auth.dto.RegisterResponse;
import com.openclassrooms.mddapi.feature.auth.dto.UserDto;

import jakarta.servlet.http.Cookie;

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
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private RefreshCookieService refreshCookieService;

	@Test
	void registerReturnsCreated() throws Exception {
		RegisterRequest request = new RegisterRequest("user@mail.com", "user", "Password1!");
		when(authService.register(any(RegisterRequest.class))).thenReturn(new RegisterResponse(1L, request.email(), request.username()));

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"user@mail.com\",\"username\":\"user\",\"password\":\"Password1!\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.email").value(request.email()));
	}

	@Test
	void loginSetsRefreshCookie() throws Exception {
		AuthService.LoginResult result = new AuthService.LoginResult("access", new UserDto(1L, "user@mail.com", "user"), "refresh");
		when(authService.login(any(LoginRequest.class))).thenReturn(result);
		when(refreshCookieService.buildRefreshCookie("refresh"))
				.thenReturn(ResponseCookie.from("refreshToken", "refresh").path("/").httpOnly(true).build());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"identifier\":\"user@mail.com\",\"password\":\"Password1!\"}"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=")))
				.andExpect(jsonPath("$.accessToken").value("access"));
	}

	@Test
	void refreshUsesCookie() throws Exception {
		AuthService.RefreshResult result = new AuthService.RefreshResult(new com.openclassrooms.mddapi.feature.auth.dto.RefreshResponse("access"), "newRefresh");
		when(authService.refresh("refresh")).thenReturn(result);
		when(refreshCookieService.buildRefreshCookie("newRefresh"))
				.thenReturn(ResponseCookie.from("refreshToken", "newRefresh").path("/").httpOnly(true).build());

		mockMvc.perform(post("/api/auth/refresh")
				.cookie(new Cookie("refreshToken", "refresh")))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=")))
				.andExpect(jsonPath("$.accessToken").value("access"));

		verify(authService).refresh(eq("refresh"));
	}

	@Test
	void logoutClearsCookie() throws Exception {
		when(refreshCookieService.clearRefreshCookie())
				.thenReturn(ResponseCookie.from("refreshToken", "").path("/").maxAge(0).build());

		mockMvc.perform(post("/api/auth/logout")
				.cookie(new Cookie("refreshToken", "refresh")))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("déconnecté")));
	}
}
