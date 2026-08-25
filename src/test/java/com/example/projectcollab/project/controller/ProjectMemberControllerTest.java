package com.example.projectcollab.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectMemberControllerTest {

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

	private Long ownerId;
	private Long newUserId;
	private Long projectId;

	@BeforeEach
	void setUp() {
		UserDto.Response owner = userService.createUser(new UserDto.CreateRequest("소유자"));
		UserDto.Response newUser = userService.createUser(new UserDto.CreateRequest("새사용자"));

		ownerId = owner.id();
		newUserId = newUser.id();

		ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("멤버십 프로젝트", "설명"));
		projectId = project.id();
	}

	@Nested
	@DisplayName("프로젝트 멤버 추가 API [POST /api/v1/projects/{projectId}/members]")
	class AddMember {

		@Test
		@DisplayName("성공: OWNER는 새 멤버를 추가할 수 있다 (201 Created)")
		void success() throws Exception {
			ProjectMemberDto.AddRequest request = new ProjectMemberDto.AddRequest(newUserId, ProjectRole.MEMBER);

			mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.userId").value(newUserId))
				.andExpect(jsonPath("$.data.role").value("MEMBER"));
		}

		@Test
		@DisplayName("실패: 이미 참여 중인 멤버를 다시 추가하면 409 Conflict를 반환한다")
		void failWhenDuplicate() throws Exception {
			projectMemberService.addMember(ownerId, projectId, new ProjectMemberDto.AddRequest(newUserId, ProjectRole.MEMBER));

			ProjectMemberDto.AddRequest duplicateRequest = new ProjectMemberDto.AddRequest(newUserId, ProjectRole.ADMIN);

			mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(duplicateRequest)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_PROJECT_MEMBER"));
		}
	}

	@Nested
	@DisplayName("프로젝트 멤버 목록 조회 API [GET /api/v1/projects/{projectId}/members]")
	class GetMembers {

		@Test
		@DisplayName("성공: 프로젝트 멤버는 멤버 목록을 조회할 수 있다 (200 OK)")
		void success() throws Exception {
			mockMvc.perform(get("/api/v1/projects/{projectId}/members", projectId)
					.header("X-User-Id", ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(1));
		}
	}

	@Nested
	@DisplayName("프로젝트 멤버 역할 변경 API [PUT /api/v1/projects/{projectId}/members/{userId}]")
	class UpdateMemberRole {

		@Test
		@DisplayName("성공: OWNER는 다른 멤버의 역할을 변경할 수 있다 (200 OK)")
		void success() throws Exception {
			projectMemberService.addMember(ownerId, projectId, new ProjectMemberDto.AddRequest(newUserId, ProjectRole.MEMBER));
			ProjectMemberDto.RoleUpdateRequest request = new ProjectMemberDto.RoleUpdateRequest(ProjectRole.ADMIN);

			mockMvc.perform(put("/api/v1/projects/{projectId}/members/{userId}", projectId, newUserId)
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.role").value("ADMIN"));
		}

		@Test
		@DisplayName("실패: 유일한 OWNER를 ADMIN으로 강등 시도 시 400 Bad Request를 반환한다")
		void failWhenDemoteLastOwner() throws Exception {
			ProjectMemberDto.RoleUpdateRequest request = new ProjectMemberDto.RoleUpdateRequest(ProjectRole.ADMIN);

			mockMvc.perform(put("/api/v1/projects/{projectId}/members/{userId}", projectId, ownerId)
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANNOT_REMOVE_LAST_OWNER"));
		}
	}

	@Nested
	@DisplayName("프로젝트 멤버 추방/탈퇴 API [DELETE /api/v1/projects/{projectId}/members/{userId}]")
	class RemoveMember {

		@Test
		@DisplayName("성공: 멤버 본인은 프로젝트를 탈퇴할 수 있다 (200 OK)")
		void successSelfLeave() throws Exception {
			projectMemberService.addMember(ownerId, projectId, new ProjectMemberDto.AddRequest(newUserId, ProjectRole.MEMBER));

			mockMvc.perform(delete("/api/v1/projects/{projectId}/members/{userId}", projectId, newUserId)
					.header("X-User-Id", newUserId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
		}

		@Test
		@DisplayName("실패: 유일한 OWNER는 탈퇴할 수 없다 (400 Bad Request)")
		void failLastOwnerLeave() throws Exception {
			mockMvc.perform(delete("/api/v1/projects/{projectId}/members/{userId}", projectId, ownerId)
					.header("X-User-Id", ownerId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANNOT_REMOVE_LAST_OWNER"));
		}
	}
}
