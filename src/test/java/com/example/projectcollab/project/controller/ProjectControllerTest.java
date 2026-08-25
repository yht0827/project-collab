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
class ProjectControllerTest {

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
	private Long memberId;
	private Long outsiderId;

	@BeforeEach
	void setUp() {
		UserDto.Response owner = userService.createUser(new UserDto.CreateRequest("프로젝트소유자"));
		UserDto.Response member = userService.createUser(new UserDto.CreateRequest("일반멤버"));
		UserDto.Response outsider = userService.createUser(new UserDto.CreateRequest("외부인"));

		ownerId = owner.id();
		memberId = member.id();
		outsiderId = outsider.id();
	}

	@Nested
	@DisplayName("프로젝트 생성 API [POST /api/v1/projects]")
	class CreateProject {

		@Test
		@DisplayName("성공: 요청자를 OWNER로 등록하며 프로젝트를 생성한다 (201 Created)")
		void success() throws Exception {
			ProjectDto.CreateRequest request = new ProjectDto.CreateRequest("신규 협업 프로젝트", "설명");

			mockMvc.perform(post("/api/v1/projects")
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("신규 협업 프로젝트"))
				.andExpect(jsonPath("$.data.id").isNumber());
		}

		@Test
		@DisplayName("실패: X-User-Id 헤더가 없으면 401 Unauthorized를 반환한다")
		void failWithoutHeader() throws Exception {
			ProjectDto.CreateRequest request = new ProjectDto.CreateRequest("프로젝트", "설명");

			mockMvc.perform(post("/api/v1/projects")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
		}
	}

	@Nested
	@DisplayName("내 프로젝트 목록 조회 API [GET /api/v1/projects]")
	class GetMyProjects {

		@Test
		@DisplayName("성공: 참여 중인 프로젝트 목록을 조회한다 (200 OK)")
		void success() throws Exception {
			projectService.createProject(ownerId, new ProjectDto.CreateRequest("소유 프로젝트 1", "설명1"));
			projectService.createProject(ownerId, new ProjectDto.CreateRequest("소유 프로젝트 2", "설명2"));

			mockMvc.perform(get("/api/v1/projects")
					.header("X-User-Id", ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(2));
		}
	}

	@Nested
	@DisplayName("프로젝트 상세 조회 API [GET /api/v1/projects/{projectId}]")
	class GetProjectDetail {

		@Test
		@DisplayName("성공: 프로젝트 멤버는 상세 정보와 내 역할을 조회할 수 있다 (200 OK)")
		void success() throws Exception {
			ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("상세 프로젝트", "설명"));
			projectMemberService.addMember(ownerId, project.id(), new ProjectMemberDto.AddRequest(memberId, ProjectRole.MEMBER));

			mockMvc.perform(get("/api/v1/projects/{projectId}", project.id())
					.header("X-User-Id", memberId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("상세 프로젝트"))
				.andExpect(jsonPath("$.data.myRole").value("MEMBER"));
		}

		@Test
		@DisplayName("실패: 멤버가 아닌 사용자가 조회를 시도하면 403 Forbidden을 반환한다")
		void failForOutsider() throws Exception {
			ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("비공개 프로젝트", "설명"));

			mockMvc.perform(get("/api/v1/projects/{projectId}", project.id())
					.header("X-User-Id", outsiderId))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("PROJECT_MEMBER_REQUIRED"));
		}
	}

	@Nested
	@DisplayName("프로젝트 정보 수정 API [PUT /api/v1/projects/{projectId}]")
	class UpdateProject {

		@Test
		@DisplayName("성공: OWNER는 프로젝트 정보를 수정할 수 있다 (200 OK)")
		void successForOwner() throws Exception {
			ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("이전 이름", "이전 설명"));
			ProjectDto.UpdateRequest request = new ProjectDto.UpdateRequest("수정된 이름", "수정된 설명");

			mockMvc.perform(put("/api/v1/projects/{projectId}", project.id())
					.header("X-User-Id", ownerId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("수정된 이름"))
				.andExpect(jsonPath("$.data.description").value("수정된 설명"));
		}

		@Test
		@DisplayName("실패: 일반 MEMBER는 프로젝트 정보를 수정할 수 없다 (403 Forbidden)")
		void failForMember() throws Exception {
			ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("원래 이름", "원래 설명"));
			projectMemberService.addMember(ownerId, project.id(), new ProjectMemberDto.AddRequest(memberId, ProjectRole.MEMBER));

			ProjectDto.UpdateRequest request = new ProjectDto.UpdateRequest("해킹 시도 이름", "설명");

			mockMvc.perform(put("/api/v1/projects/{projectId}", project.id())
					.header("X-User-Id", memberId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
		}
	}

	@Nested
	@DisplayName("프로젝트 삭제 API [DELETE /api/v1/projects/{projectId}]")
	class DeleteProject {

		@Test
		@DisplayName("성공: OWNER는 프로젝트를 삭제할 수 있다 (200 OK)")
		void successForOwner() throws Exception {
			ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("삭제할 프로젝트", "설명"));

			mockMvc.perform(delete("/api/v1/projects/{projectId}", project.id())
					.header("X-User-Id", ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
		}

		@Test
		@DisplayName("실패: ADMIN이나 MEMBER는 프로젝트를 삭제할 수 없다 (403 Forbidden)")
		void failForAdmin() throws Exception {
			ProjectDto.Response project = projectService.createProject(ownerId, new ProjectDto.CreateRequest("보호된 프로젝트", "설명"));
			projectMemberService.addMember(ownerId, project.id(), new ProjectMemberDto.AddRequest(memberId, ProjectRole.ADMIN));

			mockMvc.perform(delete("/api/v1/projects/{projectId}", project.id())
					.header("X-User-Id", memberId))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("OWNER_REQUIRED"));
		}
	}
}
