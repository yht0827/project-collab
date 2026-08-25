package com.example.projectcollab.task.service;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.project.service.ProjectMemberService;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.repository.TaskRepository;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectMemberService projectMemberService;
	private final UserService userService;

	// 작업 생성 (프로젝트 멤버 전용)
	@Transactional
	public TaskDto.Response createTask(Long currentUserId, Long projectId, TaskDto.CreateRequest request) {
		// 1. 프로젝트 조회 및 요청자 멤버십 검증
		Project project = findProjectById(projectId);
		validateMembership(projectId, currentUserId);

		// 2. 담당자 조회 및 프로젝트 멤버십 검증
		User assignee = resolveAssignee(projectId, request.assigneeId());

		// 3. 작업 생성 및 저장 후 반환
		Task task = Task.createTask(project, assignee, request.title(), request.description());
		return TaskDto.Response.from(taskRepository.save(task));
	}

	// 작업 목록 조회 (상태 필터 + 키워드 검색 + 페이징)
	public Page<TaskDto.Response> getTasks(Long currentUserId, Long projectId, TaskDto.SearchRequest condition,
		Pageable pageable) {
		// 1. 프로젝트 및 멤버십 검증
		validateProjectExists(projectId);
		validateMembership(projectId, currentUserId);

		// 2. 검색 조건 적용 및 @Query JPQL 페이징 조회
		TaskDto.SearchRequest search = Objects.requireNonNullElseGet(condition, TaskDto.SearchRequest::empty);
		return taskRepository.searchTasks(projectId, search.status(), search.keyword(), pageable)
			.map(TaskDto.Response::from);
	}

	// 작업 단건 상세 조회
	public TaskDto.Response getTask(Long currentUserId, Long projectId, Long taskId) {
		// 1. 요청자 멤버십 검증
		validateMembership(projectId, currentUserId);

		// 2. 해당 프로젝트에 속한 작업 단건 조회
		Task task = findTaskInProject(projectId, taskId);
		return TaskDto.Response.from(task);
	}

성	// 작업 수정 (담당자 본인 또는 OWNER, ADMIN / 동시 수정 시 409 Conflict)
	@Transactional
	public TaskDto.Response updateTask(Long currentUserId, Long projectId, Long taskId, TaskDto.UpdateRequest request) {
		// 1. 요청자 멤버 정보 및 작업 조회
		ProjectMember currentUserMember = projectMemberService.findMember(projectId, currentUserId);
		Task task = findTaskInProject(projectId, taskId);

		// 2. 수정 권한 검증 (담당자 본인이거나 OWNER/ADMIN 관리자만 가능)
		validateTaskModifyPermission(currentUserMember, task, currentUserId);

		// 3. 변경할 담당자 조회 및 검증
		User assignee = resolveAssignee(projectId, request.assigneeId());

		// 4. 정보 수정 (낙관적 락에 의해 동시 수정 시 409 충돌 발생)
		task.update(request.title(), request.description(), request.status(), assignee);
		return TaskDto.Response.from(task);
	}

	// 작업 삭제 (담당자 본인 또는 OWNER, ADMIN)
	@Transactional
	public void deleteTask(Long currentUserId, Long projectId, Long taskId) {
		// 1. 요청자 멤버 정보 및 작업 조회
		ProjectMember currentUserMember = projectMemberService.findMember(projectId, currentUserId);
		Task task = findTaskInProject(projectId, taskId);

		// 2. 삭제 권한 검증 (담당자 본인이거나 OWNER/ADMIN 관리자만 가능)
		validateTaskModifyPermission(currentUserMember, task, currentUserId);

		// 3. 작업 삭제
		taskRepository.delete(task);
	}

	private User resolveAssignee(Long projectId, Long assigneeId) {
		if (assigneeId == null) {
			return null;
		}
		User assignee = userService.findUserById(assigneeId);
		validateAssigneeIsProjectMember(projectId, assignee.getId());
		return assignee;
	}

	private void validateTaskModifyPermission(ProjectMember currentUserMember, Task task, Long currentUserId) {
		boolean isManager = currentUserMember.isManager();
		boolean isAssignee = task.isAssignedTo(currentUserId);

		if (!isManager && !isAssignee) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateAssigneeIsProjectMember(Long projectId, Long assigneeId) {
		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
			throw new BusinessException(ErrorCode.ASSIGNEE_NOT_PROJECT_MEMBER);
		}
	}

	private void validateMembership(Long projectId, Long userId) {
		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
			throw new BusinessException(ErrorCode.PROJECT_MEMBER_REQUIRED);
		}
	}

	private Task findTaskInProject(Long projectId, Long taskId) {
		Task task = taskRepository.findById(taskId)
			.orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

		if (!task.getProject().getId().equals(projectId)) {
			throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
		}
		return task;
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
}
