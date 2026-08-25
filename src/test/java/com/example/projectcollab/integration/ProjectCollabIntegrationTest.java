package com.example.projectcollab.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.project.dto.ProjectDto;
import com.example.projectcollab.project.dto.ProjectMemberDto;
import com.example.projectcollab.project.entity.ProjectRole;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.TaskStatus;
import com.example.projectcollab.user.dto.UserDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectCollabIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("통합 시나리오: 사용자 등록 → 프로젝트 생성 → 멤버 초대 → 작업 생성 및 수정 → 상태 필터 검색 → 프로젝트 삭제 검증")
	void fullCollaborationScenario() throws Exception {
		// 1. 사용자 2명 등록 (홍길동, 김철수)
		Long ownerId = registerUser("홍길동");
		Long memberId = registerUser("김철수");

		// 2. 프로젝트 생성 (홍길동: OWNER)
		Long projectId = createProject(ownerId, "신규 협업 프로젝트");

		// 3. 멤버 초대 (김철수: MEMBER)
		addMember(ownerId, projectId, memberId, ProjectRole.MEMBER);

		// 4. 작업 생성 (김철수 담당자로 배정)
		Long taskId = createTask(ownerId, projectId, memberId, "백엔드 API 구현");

		// 5. 작업 진행 상태 변경 (김철수가 IN_PROGRESS로 수정)
		updateTask(memberId, projectId, taskId, "백엔드 API 구현 (진행중)", TaskStatus.IN_PROGRESS);

		// 6. 작업 목록 필터 검색 (IN_PROGRESS 상태 조회)
		mockMvc.perform(get("/api/v1/projects/{projectId}/tasks", projectId)
				.header("X-User-Id", ownerId)
				.param("status", "IN_PROGRESS"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].title").value("백엔드 API 구현 (진행중)"));

		// 7. 프로젝트 삭제 (OWNER인 홍길동이 삭제)
		mockMvc.perform(delete("/api/v1/projects/{projectId}", projectId)
				.header("X-User-Id", ownerId))
			.andExpect(status().isOk());

		// 8. 삭제 후 조회 시 404 Not Found 확인
		mockMvc.perform(get("/api/v1/projects/{projectId}", projectId)
				.header("X-User-Id", ownerId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
	}

	private Long registerUser(String name) throws Exception {
		String responseJson = mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new UserDto.CreateRequest(name))))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return extractId(responseJson);
	}

	private Long createProject(Long currentUserId, String projectName) throws Exception {
		String responseJson = mockMvc.perform(post("/api/v1/projects")
				.header("X-User-Id", currentUserId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new ProjectDto.CreateRequest(projectName, "설명"))))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return extractId(responseJson);
	}

	private void addMember(Long currentUserId, Long projectId, Long targetUserId, ProjectRole role) throws Exception {
		mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
				.header("X-User-Id", currentUserId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new ProjectMemberDto.AddRequest(targetUserId, role))))
			.andExpect(status().isCreated());
	}

	private Long createTask(Long currentUserId, Long projectId, Long assigneeId, String title) throws Exception {
		String responseJson = mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
				.header("X-User-Id", currentUserId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new TaskDto.CreateRequest(title, "내용", assigneeId))))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return extractId(responseJson);
	}

	private void updateTask(Long currentUserId, Long projectId, Long taskId, String newTitle, TaskStatus status) throws
		Exception {
		mockMvc.perform(put("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, taskId)
				.header("X-User-Id", currentUserId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(new TaskDto.UpdateRequest(newTitle, "수정내용", currentUserId, status))))
			.andExpect(status().isOk());
	}

	private Long extractId(String responseJson) throws Exception {
		JsonNode root = objectMapper.readTree(responseJson);
		return root.path("data").path("id").asLong();
	}
}
