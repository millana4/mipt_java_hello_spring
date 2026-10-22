package com.example.hellospring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HelloSpringApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void messagesEndpoint() throws Exception {
		mockMvc.perform(get("/api/messages").contextPath("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].content").value("Hello, Spring!"))
				.andExpect(jsonPath("$[1].content").value("In-memory storage works"));
	}
}