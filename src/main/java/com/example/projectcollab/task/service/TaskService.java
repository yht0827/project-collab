package com.example.projectcollab.task.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.label.entity.Label;
import com.example.projectcollab.label.repository.LabelRepository;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.project.service.ProjectMemberService;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.repository.TaskRepository;
import com.example.projectcollab.task.repository.TaskSpecification;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectMemberService projectMemberService;
	private final UserService userService;
	private final LabelRepository labelRepository;

	// 신규 작업 등록
	@Transactional
	public TaskDto.Response createTask(Long currentUserId, Long projectId, TaskDto.CreateRequest request) {
		Project project = findProjectById(projectId);
		validateProjectMember(projectId, currentUserId);

		User assignee = resolveAssignee(projectId, request.assigneeId());

		Task task = Task.createTask(project, assignee, request.title(), request.description(), request.dueDate());
		Task savedTask = taskRepository.save(task);
		List<LabelDto.Response> labels = updateTaskLabels(savedTask, projectId, request.labelIds());

		return TaskDto.Response.from(savedTask, labels);
	}

	// 작업 목록 조회 (상태 필터 + 라벨 필터 + 키워드 검색 + 페이징)
	public Page<TaskDto.Response> getTasks(
		Long currentUserId,
		Long projectId,
		TaskDto.SearchRequest condition,
		Pageable pageable
	) {
		validateProjectExists(projectId);
		validateProjectMember(projectId, currentUserId);

		TaskDto.SearchRequest safeCondition = Objects.requireNonNullElseGet(condition, TaskDto.SearchRequest::empty);

		log.info("[SEARCH] 작업 검색 실행 - projectId={}, status={}, labelId={}, keyword='{}', page={}",
			projectId, safeCondition.status(), safeCondition.labelId(), (safeCondition.keyword() != null ? safeCondition.keyword() : ""), pageable.getPageNumber());

		Specification<Task> spec = TaskSpecification.searchBy(projectId, safeCondition);

		return taskRepository.findAll(spec, pageable).map(task -> {
			List<LabelDto.Response> labels = getLabelsForTask(task);
			return TaskDto.Response.from(task, labels);
		});
	}

	// 작업 단건 상세 조회
	public TaskDto.Response getTask(Long currentUserId, Long projectId, Long taskId) {
		validateProjectExists(projectId);
		validateProjectMember(projectId, currentUserId);

		Task task = findTaskByIdAndProjectId(taskId, projectId);
		List<LabelDto.Response> labels = getLabelsForTask(task);
		return TaskDto.Response.from(task, labels);
	}

	// 작업 정보 및 상태 수정
	@Transactional
	public TaskDto.Response updateTask(
		Long currentUserId,
		Long projectId,
		Long taskId,
		TaskDto.UpdateRequest request
	) {
		Task task = findTaskByIdAndProjectId(taskId, projectId);
		validateTaskModificationPermission(projectId, currentUserId, task);

		User newAssignee = resolveAssignee(projectId, request.assigneeId());
		task.update(request.title(), request.description(), request.status(), newAssignee, request.dueDate());

		List<LabelDto.Response> labels = updateTaskLabels(task, projectId, request.labelIds());

		return TaskDto.Response.from(task, labels);
	}

	// 작업 삭제
	@Transactional
	public void deleteTask(Long currentUserId, Long projectId, Long taskId) {
		Task task = findTaskByIdAndProjectId(taskId, projectId);
		validateTaskModificationPermission(projectId, currentUserId, task);

		taskRepository.delete(task);
	}

	private List<LabelDto.Response> getLabelsForTask(Task task) {
		return task.getTaskLabels()
			.stream()
			.map(tl -> LabelDto.Response.from(tl.getLabel()))
			.toList();
	}

	private List<LabelDto.Response> updateTaskLabels(Task task, Long projectId, List<Long> labelIds) {
		if (labelIds == null) {
			return getLabelsForTask(task);
		}

		if (labelIds.isEmpty()) {
			task.updateLabels(Collections.emptyList());
			return Collections.emptyList();
		}

		List<Label> labels = labelRepository.findAllById(labelIds).stream()
			.filter(label -> label.getProject().getId().equals(projectId))
			.toList();

		task.updateLabels(labels);

		return labels.stream().map(LabelDto.Response::from).toList();
	}

	private Project findProjectById(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
	}

	private void validateProjectExists(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
		}
	}

	private Task findTaskByIdAndProjectId(Long taskId, Long projectId) {
		Task task = taskRepository.findById(taskId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

		if (!task.getProject().getId().equals(projectId)) {
			throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
		}
		return task;
	}

	private void validateProjectMember(Long projectId, Long userId) {
		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
			throw new BusinessException(ErrorCode.PROJECT_MEMBER_REQUIRED);
		}
	}

	private User resolveAssignee(Long projectId, Long assigneeId) {
		if (assigneeId == null) {
			return null;
		}
		User assignee = userService.findUserById(assigneeId);
		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
			throw new BusinessException(ErrorCode.ASSIGNEE_NOT_PROJECT_MEMBER);
		}
		return assignee;
	}

	private void validateTaskModificationPermission(Long projectId, Long currentUserId, Task task) {
		if (task.isAssignedTo(currentUserId)) {
			return;
		}

		projectMemberService.validateManager(projectId, currentUserId);
	}
}
