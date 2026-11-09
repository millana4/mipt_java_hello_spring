package com.example.hellospring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HelloSpringApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void optimizedEndpointReturnsMessagesWithAuthors() throws Exception {
		mockMvc.perform(get("/api/messages/optimized").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[*].author.username", everyItem(notNullValue())));
	}

	@Test
	void nPlusOneEndpointReturnsMessagesWithAuthors() throws Exception {
		mockMvc.perform(get("/api/messages/nplus1").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[*].author.username", everyItem(notNullValue())));
	}

	@Test
	void messageEndpointReturnsDto() throws Exception {
		mockMvc.perform(get("/api/messages/fail/1").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.content", is("Hello, JPA and Hibernate!")))
				.andExpect(jsonPath("$.authorUsername", is("Alice")))
				.andExpect(jsonPath("$.author").doesNotExist());
	}

	@Test
	@Transactional // Откатывает созданное сообщение после теста
	void postCreatesMessageAndUser() throws Exception {
		String jsonPayload = "{\"content\": \"New message via POST\", \"username\": \"Tester\"}";

		mockMvc.perform(post("/api/messages").contextPath("/api")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(notNullValue())))
				.andExpect(jsonPath("$.content", is("New message via POST")))
				.andExpect(jsonPath("$.author.username", is("Tester")));

		mockMvc.perform(get("/api/messages/optimized").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));
	}

	@Test
	@Transactional
	void postWithoutUsernameUsesAnonymous() throws Exception {
		String jsonPayload = "{\"content\": \"No author\"}";

		mockMvc.perform(post("/api/messages").contextPath("/api")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.author.username", is("Anonymous")));
	}
}