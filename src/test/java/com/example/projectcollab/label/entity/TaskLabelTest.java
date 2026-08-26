package com.example.projectcollab.label.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.task.entity.Task;

class TaskLabelTest {

	@Test
	@DisplayName("성공: Task와 Label을 전달하여 TaskLabel 매핑 엔티티를 생성한다")
	void createTaskLabelSuccess() {
		Project project = Project.createProject("프로젝트", "설명");
		Task task = Task.createTask(project, null, "작업 제목", "작업 설명");
		Label label = Label.createLabel(project, "Backend", "#10b981");

		TaskLabel taskLabel = TaskLabel.createTaskLabel(task, label);

		assertThat(taskLabel.getTask()).isEqualTo(task);
		assertThat(taskLabel.getLabel()).isEqualTo(label);
	}

	@Test
	@DisplayName("실패: Task가 null이면 INVALID_INPUT_VALUE 예외가 발생한다")
	void failWhenTaskIsNull() {
		Project project = Project.createProject("프로젝트", "설명");
		Label label = Label.createLabel(project, "Backend", "#10b981");

		assertThatThrownBy(() -> TaskLabel.createTaskLabel(null, label))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("실패: Label이 null이면 INVALID_INPUT_VALUE 예외가 발생한다")
	void failWhenLabelIsNull() {
		Project project = Project.createProject("프로젝트", "설명");
		Task task = Task.createTask(project, null, "작업 제목", "작업 설명");

		assertThatThrownBy(() -> TaskLabel.createTaskLabel(task, null))
			.isInstanceOf(BusinessException.class);
	}
}
