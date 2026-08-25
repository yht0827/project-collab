package com.example.projectcollab.task.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.user.entity.User;

class TaskTest {

	@Nested
	@DisplayName("작업 생성 단위 테스트")
	class CreateTask {

		@Test
		@DisplayName("성공: 초기 상태 TODO로 작업을 생성한다")
		void success() {
			Project project = Project.createProject("프로젝트", "설명");
			User assignee = User.createUser("담당자");

			Task task = Task.createTask(project, assignee, "작업 제목", "작업 내용");

			assertThat(task.getTitle()).isEqualTo("작업 제목");
			assertThat(task.getDescription()).isEqualTo("작업 내용");
			assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
			assertThat(task.getAssignee()).isEqualTo(assignee);
		}

		@ParameterizedTest
		@NullAndEmptySource
		@DisplayName("실패: 제목이 비어있으면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenTitleIsBlank(String invalidTitle) {
			Project project = Project.createProject("프로젝트", "설명");

			assertThatThrownBy(() -> Task.createTask(project, null, invalidTitle, "설명"))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}

		@Test
		@DisplayName("실패: 제목이 최대 글자 수를 초과하면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenTitleExceedsMaxLength() {
			Project project = Project.createProject("프로젝트", "설명");
			String tooLongTitle = "a".repeat(Task.MAX_TITLE_LENGTH + 1);

			assertThatThrownBy(() -> Task.createTask(project, null, tooLongTitle, "설명"))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	@Nested
	@DisplayName("작업 수정 단위 테스트")
	class UpdateTask {

		@Test
		@DisplayName("성공: 상태와 정보가 정상 수정된다")
		void success() {
			Project project = Project.createProject("프로젝트", "설명");
			User assignee = User.createUser("담당자");
			Task task = Task.createTask(project, assignee, "이전 제목", "이전 내용");

			task.update("새 제목", "새 내용", TaskStatus.IN_PROGRESS, assignee);

			assertThat(task.getTitle()).isEqualTo("새 제목");
			assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
		}

		@Test
		@DisplayName("실패: 수정할 제목이 최대 글자 수를 초과하면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenTitleExceedsMaxLength() {
			Project project = Project.createProject("프로젝트", "설명");
			User assignee = User.createUser("담당자");
			Task task = Task.createTask(project, assignee, "이전 제목", "이전 내용");
			String tooLongTitle = "a".repeat(Task.MAX_TITLE_LENGTH + 1);

			assertThatThrownBy(() -> task.update(tooLongTitle, "새 내용", TaskStatus.IN_PROGRESS, assignee))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
