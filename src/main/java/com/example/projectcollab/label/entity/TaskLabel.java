package com.example.projectcollab.label.entity;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.task.entity.Task;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "task_labels",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_task_label", columnNames = {"task_id", "label_id"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskLabel extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "label_id", nullable = false)
	private Label label;

	private TaskLabel(Task task, Label label) {
		this.task = task;
		this.label = label;
	}

	// 작업-라벨 연결
	public static TaskLabel createTaskLabel(Task task, Label label) {
		validateTask(task);
		validateLabel(label);
		return new TaskLabel(task, label);
	}

	private static void validateTask(Task task) {
		if (task == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private static void validateLabel(Label label) {
		if (label == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
