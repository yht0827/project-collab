package com.example.projectcollab.label.entity;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.entity.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "labels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Label extends BaseTimeEntity {

	public static final int MAX_NAME_LENGTH = 30;
	public static final int MAX_COLOR_LENGTH = 20;
	public static final String DEFAULT_COLOR = "#6366f1";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(nullable = false, length = MAX_COLOR_LENGTH)
	private String color;

	private Label(Project project, String name, String color) {
		this.project = project;
		this.name = name;
		this.color = (color != null && !color.isBlank()) ? color : DEFAULT_COLOR;
	}

	// 라벨 생성
	public static Label createLabel(Project project, String name, String color) {
		validateProject(project);
		validateName(name);
		return new Label(project, name, color);
	}

	public static Label createLabel(Project project, String name) {
		return createLabel(project, name, DEFAULT_COLOR);
	}

	// 라벨 정보 수정
	public void update(String name, String color) {
		validateName(name);
		this.name = name;
		if (color != null && !color.isBlank()) {
			this.color = color;
		}
	}

	private static void validateProject(Project project) {
		if (project == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private static void validateName(String name) {
		if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
