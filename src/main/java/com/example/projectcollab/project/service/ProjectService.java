package com.example.projectcollab.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.label.repository.LabelRepository;
import com.example.projectcollab.project.dto.ProjectDto;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.task.repository.TaskRepository;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectMemberService projectMemberService;
	private final TaskRepository taskRepository;
	private final UserService userService;
	private final LabelRepository labelRepository;

	// 프로젝트 생성 (생성자 OWNER 자동 등록)
	@Transactional
	public ProjectDto.Response createProject(Long currentUserId, ProjectDto.CreateRequest request) {
		User currentUser = userService.findUserById(currentUserId);

		Project project = Project.createProject(request.name(), request.description());
		Project savedProject = projectRepository.save(project);

		projectMemberService.registerOwner(savedProject, currentUser);

		return ProjectDto.Response.from(savedProject);
	}

	// 내 참여 프로젝트 목록 조회
	public List<ProjectDto.Response> getMyProjects(Long currentUserId) {
		userService.findUserById(currentUserId);
		List<ProjectMember> memberships = projectMemberRepository.findAllByUserIdWithProject(currentUserId);

		return memberships.stream()
			.map(membership -> ProjectDto.Response.from(membership.getProject()))
			.toList();
	}

	// 프로젝트 상세 조회 (멤버 전용)
	public ProjectDto.DetailResponse getProjectDetail(Long currentUserId, Long projectId) {
		Project project = findProjectById(projectId);
		ProjectMember member = findProjectMember(projectId, currentUserId);

		return ProjectDto.DetailResponse.of(project, member.getRole());
	}

	// 프로젝트 정보 수정 (OWNER, ADMIN 전용)
	@Transactional
	public ProjectDto.Response updateProject(Long currentUserId, Long projectId, ProjectDto.UpdateRequest request) {
		Project project = findProjectById(projectId);
		ProjectMember member = findProjectMember(projectId, currentUserId);

		if (!member.isManager()) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		project.update(request.name(), request.description());
		return ProjectDto.Response.from(project);
	}

	// 프로젝트 삭제 (OWNER 전용)
	@Transactional
	public void deleteProject(Long currentUserId, Long projectId) {
		Project project = findProjectById(projectId);
		ProjectMember member = findProjectMember(projectId, currentUserId);

		if (!member.isOwner()) {
			throw new BusinessException(ErrorCode.OWNER_REQUIRED);
		}

		labelRepository.deleteAllByProjectId(projectId);
		taskRepository.deleteAllByProjectId(projectId);
		projectMemberRepository.deleteAllByProjectId(projectId);

		projectRepository.delete(project);
	}

	public Project findProjectById(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
	}

	public ProjectMember findProjectMember(Long projectId, Long userId) {
		return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_REQUIRED));
	}
}
