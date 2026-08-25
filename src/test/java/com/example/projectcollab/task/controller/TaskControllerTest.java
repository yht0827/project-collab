package com.example.projectcollab.task.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import com.example.projectcollab.project.service.ProjectMemberService;
import com.example.projectcollab.project.service.ProjectService;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.TaskStatus;
import com.example.projectcollab.task.service.TaskService;
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserService userService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectMemberService projectMemberService;

	@Autowired
	private TaskService taskService;

	private Long ownerId;
	private Long memberId;
	private Long outsiderId;
	private Long projectId;

	@BeforeEach
	void setUp() {
		UserDto.Response owner = userService.createUser(new UserDto.CreateRequest("소유자"));
		UserDto.Response member = userService.createUser(new UserDto.CreateRequest("멤버"));
		UserDto.Response outsider = userService.createUser(new UserDto.CreateRequest("외부인"));

		ownerId = owner.id();
		memberId = member.id();
		outsiderId = outsider.id();

		ProjectDto.Response project = projectService.createProject(ownerId,
			new ProjectDto.CreateRequest("작업 프로젝트", "설명"));
		projectId = project.id();

		projectMemberService.addMember(ownerId, projectId,
			new ProjectMemberDto.AddRequest(memberId, ProjectRole.MEMBER));
	}

	@Nested
	@DisplayName("작업 생성 API [POST /api/v1/projects/{projectId}/tasks]")
	class CreateTask {

		@Test
		@DisplayName("성공: 프로젝트 멤버는 작업을 생성할 수 있다 (201 Created)")
		void success() throws Exception {
			TaskDto.CreateRequest request = new TaskDto.CreateRequest("신규 작업", "작업 내용", memberId);

			mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
					.header("X-User-Id", memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.title").value("신규 작업"))
				.andExpect(jsonPath("$.data.status").value("TODO"))
				.andExpect(jsonPath("$.data.assigneeId").value(memberId));
		}

		@Test
		@DisplayName("실패: 외부인을 담당자로 지정하면 400 Bad Request를 반환한다")
		void failWhenAssigneeNotMember() throws Exception {
			TaskDto.CreateRequest request = new TaskDto.CreateRequest("신규 작업", "작업 내용", outsiderId);

			mockMvc.perform(post("/api/v1/projects/{projectId}/tasks", projectId)
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ASSIGNEE_NOT_PROJECT_MEMBER"));
		}
	}

	@Nested
	@DisplayName("작업 목록 조회 API [GET /api/v1/projects/{projectId}/tasks]")
	class GetTasks {

		@Test
		@DisplayName("성공: 상태 필터와 검색어를 적용하여 페이징 조회한다 (200 OK)")
		void successWithFilter() throws Exception {
			taskService.createTask(ownerId, projectId, new TaskDto.CreateRequest("Spring Boot 구현", "설명", null));
			taskService.createTask(ownerId, projectId, new TaskDto.CreateRequest("React 프론트 구현", "설명", null));

			mockMvc.perform(get("/api/v1/projects/{projectId}/tasks", projectId)
					.header("X-User-Id", memberId)
					.param("keyword", "Spring")
					.param("status", "TODO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].title").value("Spring Boot 구현"));
		}
	}

	@Nested
	@DisplayName("작업 수정 API [PUT /api/v1/projects/{projectId}/tasks/{taskId}]")
	class UpdateTask {

		@Test
		@DisplayName("성공: 담당자 본인은 작업을 수정할 수 있다 (200 OK)")
		void successForAssignee() throws Exception {
			TaskDto.Response task = taskService.createTask(ownerId, projectId,
				new TaskDto.CreateRequest("작업 제목", "작업 설명", memberId));
			TaskDto.UpdateRequest request = new TaskDto.UpdateRequest("수정된 제목", "수정된 내용", memberId,
				TaskStatus.IN_PROGRESS);

			mockMvc.perform(put("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, task.id())
					.header("X-User-Id", memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("수정된 제목"))
				.andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
		}

		@Test
		@DisplayName("실패: 담당자가 아닌 다른 일반 멤버는 수정할 수 없다 (403 Forbidden)")
		void failForOtherMember() throws Exception {
			TaskDto.Response task = taskService.createTask(ownerId, projectId,
				new TaskDto.CreateRequest("소유자 담당 작업", "설명", ownerId));
			TaskDto.UpdateRequest request = new TaskDto.UpdateRequest("수정 시도", "설명", ownerId, TaskStatus.DONE);

			mockMvc.perform(put("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, task.id())
					.header("X-User-Id", memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
		}
	}

	@Nested
	@DisplayName("작업 삭제 API [DELETE /api/v1/projects/{projectId}/tasks/{taskId}]")
	class DeleteTask {

		@Test
		@DisplayName("성공: OWNER는 작업을 삭제할 수 있다 (200 OK)")
		void successForOwner() throws Exception {
			TaskDto.Response task = taskService.createTask(ownerId, projectId,
				new TaskDto.CreateRequest("삭제할 작업", "설명", memberId));

			mockMvc.perform(delete("/api/v1/projects/{projectId}/tasks/{taskId}", projectId, task.id())
					.header("X-User-Id", ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
		}
	}
}
