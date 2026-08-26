package com.example.projectcollab.label.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.label.service.LabelService;
import com.example.projectcollab.project.dto.ProjectDto;
import com.example.projectcollab.project.service.ProjectService;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.service.TaskService;
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LabelControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserService userService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private LabelService labelService;

	private Long ownerId;
	private Long projectId;

	@BeforeEach
	void setUp() {
		UserDto.Response user = userService.createUser(new UserDto.CreateRequest("라벨테스터"));
		ownerId = user.id();
		ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("라벨프로젝트", "설명"));
		projectId = project.id();
	}

	@Test
	@DisplayName("라벨 생성 API - 201 Created")
	void createLabel() throws Exception {
		LabelDto.CreateRequest request = new LabelDto.CreateRequest("Backend", "#10b981");

		mockMvc.perform(post("/api/v1/projects/{projectId}/labels", projectId)
				.header("X-User-Id", ownerId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.name").value("Backend"))
			.andExpect(jsonPath("$.data.color").value("#10b981"));
	}

	@Test
	@DisplayName("라벨 목록 조회 API - 200 OK")
	void getLabels() throws Exception {
		labelService.createLabel(ownerId, projectId, new LabelDto.CreateRequest("Frontend", "#3b82f6"));

		mockMvc.perform(get("/api/v1/projects/{projectId}/labels", projectId)
				.header("X-User-Id", ownerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].name").value("Frontend"));
	}

	@Test
	@DisplayName("라벨 삭제 API - 200 OK (작업에 할당되어 있어도 외래키 충돌 없이 정상 삭제)")
	void deleteLabelAssignedToTask() throws Exception {
		LabelDto.Response label = labelService.createLabel(ownerId, projectId, new LabelDto.CreateRequest("Bug", "#ef4444"));

		// 해당 라벨을 할당받은 작업 생성
		taskService.createTask(ownerId, projectId, new TaskDto.CreateRequest(
			"버그 작업", "버그 수정 설명", ownerId, null, List.of(label.id())
		));

		// 라벨 삭제 요청
		mockMvc.perform(delete("/api/v1/projects/{projectId}/labels/{labelId}", projectId, label.id())
				.header("X-User-Id", ownerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}
}
