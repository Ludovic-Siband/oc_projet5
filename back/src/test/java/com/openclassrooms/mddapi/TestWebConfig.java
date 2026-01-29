package com.openclassrooms.mddapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@TestConfiguration
public class TestWebConfig {

	@Bean
	public MockMvc mockMvc(WebApplicationContext context) {
		return MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Bean
	@Order(0)
	public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/**")
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}
}
