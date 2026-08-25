package com.example.projectcollab.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Nested
	@DisplayName("사용자 등록 API [POST /api/v1/users]")
	class CreateUser {

		@Test
		@DisplayName("성공: 유효한 이름으로 사용자를 등록하면 201 Created를 반환한다")
		void success() throws Exception {
			UserDto.CreateRequest request = new UserDto.CreateRequest("홍길동");

			mockMvc.perform(post("/api/v1/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("홍길동"))
				.andExpect(jsonPath("$.data.id").isNumber());
		}

		@Test
		@DisplayName("실패: 이름이 공백이면 400 Bad Request를 반환한다")
		void failWhenNameIsBlank() throws Exception {
			UserDto.CreateRequest request = new UserDto.CreateRequest("  ");

			mockMvc.perform(post("/api/v1/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
		}

		@Test
		@DisplayName("실패: 이름이 최대 글자 수를 초과하면 400 Bad Request를 반환한다")
		void failWhenNameExceedsMaxLength() throws Exception {
			String tooLongName = "a".repeat(User.MAX_NAME_LENGTH + 1);
			UserDto.CreateRequest request = new UserDto.CreateRequest(tooLongName);

			mockMvc.perform(post("/api/v1/users")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
		}
	}

	@Nested
	@DisplayName("사용자 단건 조회 API [GET /api/v1/users/{userId}]")
	class GetUser {

		@Test
		@DisplayName("성공: 존재하는 사용자를 조회하면 200 OK와 사용자 정보를 반환한다")
		void success() throws Exception {
			User user = userRepository.save(User.createUser("김철수"));

			mockMvc.perform(get("/api/v1/users/{userId}", user.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(user.getId()))
				.andExpect(jsonPath("$.data.name").value("김철수"));
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 ID로 조회하면 404 Not Found를 반환한다")
		void failWhenUserNotFound() throws Exception {
			mockMvc.perform(get("/api/v1/users/{userId}", 99999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
		}
	}
}
