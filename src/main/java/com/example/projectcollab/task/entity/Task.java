package com.example.projectcollab.task.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.label.entity.Label;
import com.example.projectcollab.label.entity.TaskLabel;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.user.entity.User;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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

	@Column(name = "due_date")
	private LocalDate dueDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TaskLabel> taskLabels = new ArrayList<>();

	@Version
	private Long version;

	private Task(Project project, User assignee, String title, String description, LocalDate dueDate, TaskStatus status) {
		this.project = project;
		this.assignee = assignee;
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.status = (status != null) ? status : TaskStatus.TODO;
	}

	// 신규 작업 생성
	public static Task createTask(Project project, User assignee, String title, String description) {
		return createTask(project, assignee, title, description, null);
	}

	public static Task createTask(Project project, User assignee, String title, String description, LocalDate dueDate) {
		if (project == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		validateTitle(title);
		return new Task(project, assignee, title, description, dueDate, TaskStatus.TODO);
	}

	// 작업 전체 수정 (제목, 설명, 상태, 담당자, 마감일)
	public void update(String title, String description, TaskStatus status, User assignee) {
		update(title, description, status, assignee, this.dueDate);
	}

	public void update(String title, String description, TaskStatus status, User assignee, LocalDate dueDate) {
		validateTitle(title);
		if (status == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		this.title = title;
		this.description = description;
		this.status = status;
		this.assignee = assignee;
		this.dueDate = dueDate;
	}

	// 라벨 컬렉션 동기화 (유니크 제약 충돌 방지 차분 동기화)
	public void updateLabels(List<Label> newLabels) {
		if (newLabels == null || newLabels.isEmpty()) {
			this.taskLabels.clear();
			return;
		}

		Set<Long> newLabelIds = newLabels.stream().map(Label::getId).collect(Collectors.toSet());
		// 1. 제거 대상 삭제
		this.taskLabels.removeIf(tl -> !newLabelIds.contains(tl.getLabel().getId()));

		// 2. 신규 라벨만 추가
		Set<Long> existingLabelIds = this.taskLabels.stream().map(tl -> tl.getLabel().getId()).collect(Collectors.toSet());
		for (Label label : newLabels) {
			if (!existingLabelIds.contains(label.getId())) {
				this.taskLabels.add(TaskLabel.createTaskLabel(this, label));
			}
		}
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
