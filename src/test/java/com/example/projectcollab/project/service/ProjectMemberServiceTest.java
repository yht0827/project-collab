package com.example.projectcollab.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import com.example.projectcollab.project.dto.ProjectMemberDto;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.entity.ProjectRole;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

	@InjectMocks
	private ProjectMemberService projectMemberService;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private ProjectMemberRepository projectMemberRepository;

	@Mock
	private UserService userService;

	@Nested
	@DisplayName("멤버 추가 단위 테스트")
	class AddMember {

		@Test
		@DisplayName("성공: OWNER는 새 멤버를 추가할 수 있다")
		void success() {
			Long projectId = 100L;
			Long ownerId = 1L;
			Long targetUserId = 2L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);
			User targetUser = User.createUser("대상자");
			ReflectionTestUtils.setField(targetUser, "id", targetUserId);

			ProjectMember ownerMember = ProjectMember.createOwner(project, owner);
			ProjectMember newMember = ProjectMember.createWithRole(project, targetUser, ProjectRole.MEMBER);

			ProjectMemberDto.AddRequest request = new ProjectMemberDto.AddRequest(targetUserId, ProjectRole.MEMBER);

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(ownerMember));
			given(userService.findUserById(targetUserId)).willReturn(targetUser);
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUserId)).willReturn(false);
			given(projectMemberRepository.save(any(ProjectMember.class))).willReturn(newMember);

			ProjectMemberDto.Response response = projectMemberService.addMember(ownerId, projectId, request);

			assertThat(response.userId()).isEqualTo(targetUserId);
			assertThat(response.role()).isEqualTo(ProjectRole.MEMBER);
		}

		@Test
		@DisplayName("실패: 이미 등록된 멤버를 추가하면 DUPLICATE_PROJECT_MEMBER 예외가 발생한다")
		void failWhenDuplicate() {
			Long projectId = 100L;
			Long ownerId = 1L;
			Long targetUserId = 2L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);
			User targetUser = User.createUser("대상자");
			ReflectionTestUtils.setField(targetUser, "id", targetUserId);

			ProjectMember ownerMember = ProjectMember.createOwner(project, owner);

			ProjectMemberDto.AddRequest request = new ProjectMemberDto.AddRequest(targetUserId, ProjectRole.MEMBER);

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(ownerMember));
			given(userService.findUserById(targetUserId)).willReturn(targetUser);
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUserId)).willReturn(true);

			assertThatThrownBy(() -> projectMemberService.addMember(ownerId, projectId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PROJECT_MEMBER);
		}
	}

	@Nested
	@DisplayName("멤버 역할 변경 단위 테스트")
	class UpdateMemberRole {

		@Test
		@DisplayName("성공: OWNER는 다른 멤버의 역할을 ADMIN으로 변경할 수 있다")
		void success() {
			Long projectId = 100L;
			Long ownerId = 1L;
			Long targetUserId = 2L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);
			User targetUser = User.createUser("대상자");
			ReflectionTestUtils.setField(targetUser, "id", targetUserId);

			ProjectMember ownerMember = ProjectMember.createOwner(project, owner);
			ProjectMember targetMember = ProjectMember.createWithRole(project, targetUser, ProjectRole.MEMBER);

			ProjectMemberDto.RoleUpdateRequest request = new ProjectMemberDto.RoleUpdateRequest(ProjectRole.ADMIN);

			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(ownerMember));
			given(projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)).willReturn(Optional.of(targetMember));

			ProjectMemberDto.Response response = projectMemberService.updateMemberRole(ownerId, projectId, targetUserId, request);

			assertThat(response.role()).isEqualTo(ProjectRole.ADMIN);
		}

		@Test
		@DisplayName("실패: 유일한 OWNER를 다른 역할로 강등 시도 시 CANNOT_REMOVE_LAST_OWNER 예외가 발생한다")
		void failWhenDemoteLastOwner() {
			Long projectId = 100L;
			Long ownerId = 1L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);

			ProjectMember ownerMember = ProjectMember.createOwner(project, owner);
			ProjectMemberDto.RoleUpdateRequest request = new ProjectMemberDto.RoleUpdateRequest(ProjectRole.ADMIN);

			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(ownerMember));
			given(projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER)).willReturn(1L);

			assertThatThrownBy(() -> projectMemberService.updateMemberRole(ownerId, projectId, ownerId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_REMOVE_LAST_OWNER);
		}
	}

	@Nested
	@DisplayName("멤버 추방 및 탈퇴 단위 테스트")
	class RemoveMember {

		@Test
		@DisplayName("성공: 일반 멤버 본인은 탈퇴할 수 있다")
		void successSelfLeave() {
			Long projectId = 100L;
			Long memberId = 2L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User memberUser = User.createUser("일반멤버");
			ReflectionTestUtils.setField(memberUser, "id", memberId);

			ProjectMember member = ProjectMember.createMember(project, memberUser);

			given(projectMemberRepository.findByProjectIdAndUserId(projectId, memberId)).willReturn(Optional.of(member));

			projectMemberService.removeMember(memberId, projectId, memberId);

			verify(projectMemberRepository).delete(member);
		}

		@Test
		@DisplayName("실패: 유일한 OWNER는 탈퇴할 수 없다")
		void failLastOwnerLeave() {
			Long projectId = 100L;
			Long ownerId = 1L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User owner = User.createUser("소유자");
			ReflectionTestUtils.setField(owner, "id", ownerId);

			ProjectMember ownerMember = ProjectMember.createOwner(project, owner);

			given(projectMemberRepository.findByProjectIdAndUserId(projectId, ownerId)).willReturn(Optional.of(ownerMember));
			given(projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER)).willReturn(1L);

			assertThatThrownBy(() -> projectMemberService.removeMember(ownerId, projectId, ownerId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_REMOVE_LAST_OWNER);
		}
	}
}
