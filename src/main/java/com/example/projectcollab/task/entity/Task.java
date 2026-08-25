package com.example.projectcollab.task.entity;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseTimeEntity {

	public static final int MAX_TITLE_LENGTH = 200;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = MAX_TITLE_LENGTH)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@Version
	private Long version;

	private Task(Project project, User assignee, String title, String description, TaskStatus status) {
		this.project = project;
		this.assignee = assignee;
		this.title = title;
		this.description = description;
		this.status = (status != null) ? status : TaskStatus.TODO;
	}

	// 신규 작업 생성
	public static Task createTask(Project project, User assignee, String title, String description) {
		if (project == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		validateTitle(title);
		return new Task(project, assignee, title, description, TaskStatus.TODO);
	}

	// 작업 전체 수정 (제목, 설명, 상태, 담당자)
	public void update(String title, String description, TaskStatus status, User assignee) {
		validateTitle(title);
		if (status == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		this.title = title;
		this.description = description;
		this.status = status;
		this.assignee = assignee;
	}

	// 작업 진행 상태 변경
	public void updateStatus(TaskStatus status) {
		if (status == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		this.status = status;
	}

	// 해당 사용자가 작업의 담당자인지 확인
	public boolean isAssignedTo(Long userId) {
		return this.assignee != null && this.assignee.getId().equals(userId);
	}

	private static void validateTitle(String title) {
		if (title == null || title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
