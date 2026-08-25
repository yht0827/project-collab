package com.example.projectcollab.project.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.user.entity.User;

class ProjectMemberTest {

	@Nested
	@DisplayName("ProjectMember 생성 단위 테스트")
	class CreateMember {

		@Test
		@DisplayName("성공: OWNER 멤버를 정상 생성한다")
		void successOwner() {
			// given
			Project project = Project.createProject("프로젝트", "설명");
			User user = User.createUser("사용자");

			// when
			ProjectMember member = ProjectMember.createOwner(project, user);

			// then
			assertThat(member.getRole()).isEqualTo(ProjectRole.OWNER);
			assertThat(member.isOwner()).isTrue();
			assertThat(member.isManager()).isTrue();
		}

		@Test
		@DisplayName("성공: 일반 MEMBER를 정상 생성한다")
		void successMember() {
			// given
			Project project = Project.createProject("프로젝트", "설명");
			User user = User.createUser("사용자");

			// when
			ProjectMember member = ProjectMember.createMember(project, user);

			// then
			assertThat(member.getRole()).isEqualTo(ProjectRole.MEMBER);
			assertThat(member.isOwner()).isFalse();
			assertThat(member.isManager()).isFalse();
		}

		@Test
		@DisplayName("실패: 프로젝트나 사용자가 null이면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenNull() {
			User user = User.createUser("사용자");

			assertThatThrownBy(() -> ProjectMember.createOwner(null, user))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	@Nested
	@DisplayName("ProjectMember 역할 수정 단위 테스트")
	class UpdateRole {

		@Test
		@DisplayName("성공: 역할을 ADMIN으로 변경한다")
		void success() {
			// given
			Project project = Project.createProject("프로젝트", "설명");
			User user = User.createUser("사용자");
			ProjectMember member = ProjectMember.createMember(project, user);

			// when
			member.updateRole(ProjectRole.ADMIN);

			// then
			assertThat(member.getRole()).isEqualTo(ProjectRole.ADMIN);
			assertThat(member.isManager()).isTrue();
		}
	}
}
