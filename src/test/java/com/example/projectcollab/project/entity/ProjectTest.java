package com.example.projectcollab.project.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;

class ProjectTest {

	@Nested
	@DisplayName("프로젝트 생성 단위 테스트")
	class CreateProjectTest {

		@Test
		@DisplayName("성공: 유효한 이름과 설명으로 프로젝트를 생성한다")
		void success() {
			Project project = Project.createProject("신규 프로젝트", "프로젝트 설명");

			assertThat(project.getName()).isEqualTo("신규 프로젝트");
			assertThat(project.getDescription()).isEqualTo("프로젝트 설명");
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {"   ", "\t", "\n"})
		@DisplayName("실패: 이름이 null이거나 공백이면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenNameIsBlank(String invalidName) {
			assertThatThrownBy(() -> Project.createProject(invalidName, "설명"))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}

		@Test
		@DisplayName("실패: 이름이 최대 글자 수를 초과하면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenNameExceedsMaxLength() {
			String tooLongName = "a".repeat(Project.MAX_NAME_LENGTH + 1);
			assertThatThrownBy(() -> Project.createProject(tooLongName, "설명"))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	@Nested
	@DisplayName("프로젝트 수정 단위 테스트")
	class UpdateProjectTest {

		@Test
		@DisplayName("성공: 이름과 설명을 수정한다")
		void success() {
			Project project = Project.createProject("이전 이름", "이전 설명");

			project.update("새 이름", "새 설명");

			assertThat(project.getName()).isEqualTo("새 이름");
			assertThat(project.getDescription()).isEqualTo("새 설명");
		}

		@Test
		@DisplayName("실패: 수정할 이름이 최대 글자 수를 초과하면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenNameExceedsMaxLength() {
			Project project = Project.createProject("기존 이름", "설명");
			String tooLongName = "a".repeat(Project.MAX_NAME_LENGTH + 1);

			assertThatThrownBy(() -> project.update(tooLongName, "새 설명"))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
