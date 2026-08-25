package com.example.projectcollab.project.entity;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {

	public static final int MAX_NAME_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	private Project(String name, String description) {
		this.name = name;
		this.description = description;
	}

	// 프로젝트 생성
	public static Project createProject(String name, String description) {
		validateName(name);
		return new Project(name, description);
	}

	// 프로젝트 정보(이름, 설명) 수정
	public void update(String name, String description) {
		validateName(name);
		this.name = name;
		this.description = description;
	}

	private static void validateName(String name) {
		if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
