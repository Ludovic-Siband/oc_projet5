package com.openclassrooms.mddapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.docker.compose.enabled=false",
		"app.auth.jwt-secret=0123456789abcdef0123456789abcdef"
})
class MddApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
