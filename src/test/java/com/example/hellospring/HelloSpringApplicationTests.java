package com.example.hellospring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HelloSpringApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void getMessagesEndpointReturnsInitialData() throws Exception {
		mockMvc.perform(get("/api/messages").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2))) // Проверка, что 2 тестовых сообщения загружены
				.andExpect(jsonPath("$[0].author", is(notNullValue()))) // Проверка наличия поля «author»
				.andExpect(jsonPath("$[0].content", anyOf(
						is("Hello, JDBC Spring!"),
						is("Now using Postgres database via JdbcTemplate."))
				));
	}

	@Test
	void postMessagesEndpointCreatesNewMessage() throws Exception {
		String jsonPayload = "{\"content\": \"New message via POST\", \"author\": \"Tester\"}";

		// 1. Отправка POST-запроса
		mockMvc.perform(post("/api/messages").contextPath("/api")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isCreated()) // Ожидаем 201 Created
				.andExpect(jsonPath("$.id", is(notNullValue()))) // Проверяем, что ID сгенерирован
				.andExpect(jsonPath("$.author", is("Tester")))
				.andExpect(jsonPath("$.content", is("New message via POST")));

		// 2. Проверка увеличения общего количества сообщений (было 2 — стало 3)
		mockMvc.perform(get("/api/messages").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)));
	}
}