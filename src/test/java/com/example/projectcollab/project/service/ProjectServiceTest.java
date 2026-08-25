package com.example.projectcollab.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.dto.ProjectDto;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.entity.ProjectRole;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.task.repository.TaskRepository;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

	@InjectMocks
	private ProjectService projectService;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private ProjectMemberRepository projectMemberRepository;

	@Mock
	private ProjectMemberService projectMemberService;

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private UserService userService;

	@Nested
	@DisplayName("프로젝트 생성 단위 테스트")
	class CreateProject {

		@Test
		@DisplayName("성공: 프로젝트를 저장하고 생성자를 OWNER로 등록한다")
		void success() {
			// given
			Long userId = 1L;
			User user = User.createUser("홍길동");
			ReflectionTestUtils.setField(user, "id", userId);

			ProjectDto.CreateRequest request = new ProjectDto.CreateRequest("신규 프로젝트", "설명");
			Project project = Project.createProject(request.name(), request.description());
			ReflectionTestUtils.setField(project, "id", 100L);

			given(userService.findUserById(userId)).willReturn(user);
			given(projectRepository.save(any(Project.class))).willReturn(project);

			// when
			ProjectDto.Response response = projectService.createProject(userId, request);

			// then
			assertThat(response.id()).isEqualTo(100L);
			assertThat(response.name()).isEqualTo("신규 프로젝트");
			verify(projectMemberService).registerOwner(project, user);
		}
	}

	@Nested
	@DisplayName("내 프로젝트 목록 조회 단위 테스트")
	class GetMyProjects {

		@Test
		@DisplayName("성공: 내가 속한 프로젝트 목록을 반환한다")
		void success() {
			// given
			Long userId = 1L;
			User user = User.createUser("홍길동");
			ReflectionTestUtils.setField(user, "id", userId);

			Project project = Project.createProject("프로젝트 1", "설명");
			ReflectionTestUtils.setField(project, "id", 10L);
			ProjectMember member = ProjectMember.createOwner(project, user);

			given(userService.findUserById(userId)).willReturn(user);
			given(projectMemberRepository.findAllByUserIdWithProject(userId)).willReturn(List.of(member));

			// when
			List<ProjectDto.Response> responses = projectService.getMyProjects(userId);

			// then
			assertThat(responses).hasSize(1);
			assertThat(responses.get(0).id()).isEqualTo(10L);
		}
	}

	@Nested
	@DisplayName("프로젝트 정보 수정 단위 테스트")
	class UpdateProject {

		@Test
		@DisplayName("성공: 관리자(OWNER/ADMIN)는 프로젝트 정보를 수정할 수 있다")
		void success() {
			// given
			Long projectId = 10L;
			Long ownerId = 1L;
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);

			Project project = Project.createProject("이전 이름", "이전 설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			ProjectMember member = ProjectMember.createOwner(project, owner);

			ProjectDto.UpdateRequest request = new ProjectDto.UpdateRequest("새 이름", "새 설명");

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(member));

			// when
			ProjectDto.Response response = projectService.updateProject(ownerId, projectId, request);

			// then
			assertThat(response.name()).isEqualTo("새 이름");
			assertThat(response.description()).isEqualTo("새 설명");
		}

		@Test
		@DisplayName("실패: 일반 MEMBER는 수정 시 ACCESS_DENIED 예외가 발생한다")
		void failForMember() {
			// given
			Long projectId = 10L;
			Long memberId = 2L;
			User memberUser = User.createUser("일반멤버");
			ReflectionTestUtils.setField(memberUser, "id", memberId);

			Project project = Project.createProject("이름", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			ProjectMember member = ProjectMember.createMember(project, memberUser);

			ProjectDto.UpdateRequest request = new ProjectDto.UpdateRequest("새 이름", "새 설명");

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, memberId)).willReturn(Optional.of(member));

			// when & then
			assertThatThrownBy(() -> projectService.updateProject(memberId, projectId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
		}
	}

	@Nested
	@DisplayName("프로젝트 삭제 단위 테스트")
	class DeleteProject {

		@Test
		@DisplayName("성공: OWNER는 프로젝트를 삭제할 수 있다")
		void success() {
			// given
			Long projectId = 10L;
			Long ownerId = 1L;
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);

			Project project = Project.createProject("삭제 프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			ProjectMember member = ProjectMember.createOwner(project, owner);

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(member));

			// when
			projectService.deleteProject(ownerId, projectId);

			// then
			verify(taskRepository).deleteAllByProjectId(projectId);
			verify(projectMemberRepository).deleteAllByProjectId(projectId);
			verify(projectRepository).delete(project);
		}

		@Test
		@DisplayName("실패: ADMIN은 삭제 시 OWNER_REQUIRED 예외가 발생한다")
		void failForAdmin() {
			// given
			Long projectId = 10L;
			Long adminId = 2L;
			User adminUser = User.createUser("관리자");
			ReflectionTestUtils.setField(adminUser, "id", adminId);

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			ProjectMember member = ProjectMember.createWithRole(project, adminUser, ProjectRole.ADMIN);

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, adminId)).willReturn(Optional.of(member));

			// when & then
			assertThatThrownBy(() -> projectService.deleteProject(adminId, projectId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.OWNER_REQUIRED);
		}
	}
}
