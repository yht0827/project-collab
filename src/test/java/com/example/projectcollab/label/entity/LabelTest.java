package com.example.projectcollab.label.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.project.entity.Project;

class LabelTest {

	@Test
	@DisplayName("라벨 생성 시 기본 컬러가 주어지지 않으면 기본 인디고 컬러(#6366f1)가 적용된다")
	void createLabelWithDefaultColor() {
		Project project = Project.createProject("프로젝트", "설명");
		Label label = Label.createLabel(project, "Backend", null);

		assertThat(label.getName()).isEqualTo("Backend");
		assertThat(label.getColor()).isEqualTo("#6366f1");
		assertThat(label.getProject()).isEqualTo(project);
	}

	@Test
	@DisplayName("라벨 생성 시 이름이 없으면 예외가 발생한다")
	void createLabelWithoutName() {
		Project project = Project.createProject("프로젝트", "설명");

		assertThatThrownBy(() -> Label.createLabel(project, ""))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("라벨 수정 시 이름과 색상이 변경된다")
	void updateLabel() {
		Project project = Project.createProject("프로젝트", "설명");
		Label label = Label.createLabel(project, "Backend", "#10b981");

		label.update("Frontend", "#3b82f6");

		assertThat(label.getName()).isEqualTo("Frontend");
		assertThat(label.getColor()).isEqualTo("#3b82f6");
	}
}
