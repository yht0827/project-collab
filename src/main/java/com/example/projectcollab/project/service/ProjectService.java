package com.example.projectcollab.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
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

	// 프로젝트 생성 (생성자 OWNER 자동 등록)
	@Transactional
	public ProjectDto.Response createProject(Long currentUserId, ProjectDto.CreateRequest request) {
		// 1. 요청 사용자 조회
		User currentUser = userService.findUserById(currentUserId);

		// 2. 프로젝트 엔티티 생성 및 저장
		Project project = Project.createProject(request.name(), request.description());
		Project savedProject = projectRepository.save(project);

		// 3. 생성자를 해당 프로젝트의 OWNER로 등록 위임
		projectMemberService.registerOwner(savedProject, currentUser);

		return ProjectDto.Response.from(savedProject);
	}

	// 내 참여 프로젝트 목록 조회
	public List<ProjectDto.Response> getMyProjects(Long currentUserId) {
		// 1. 사용자 존재 여부 확인
		userService.findUserById(currentUserId);

		// 2. 참여 중인 프로젝트 멤버십 조회 (Fetch Join)
		List<ProjectMember> memberships = projectMemberRepository.findAllByUserIdWithProject(currentUserId);

		// 3. DTO 변환 후 반환
		return memberships.stream()
			.map(membership -> ProjectDto.Response.from(membership.getProject()))
			.toList();
	}

	// 프로젝트 상세 조회 (멤버 전용)
	public ProjectDto.DetailResponse getProjectDetail(Long currentUserId, Long projectId) {
		// 1. 프로젝트 및 요청자 멤버십 조회 (비멤버 시 403)
		Project project = findProjectById(projectId);
		ProjectMember member = findProjectMember(projectId, currentUserId);

		// 2. 프로젝트 정보와 내 역할 반환
		return ProjectDto.DetailResponse.of(project, member.getRole());
	}

	// 프로젝트 정보 수정 (OWNER, ADMIN 전용)
	@Transactional
	public ProjectDto.Response updateProject(Long currentUserId, Long projectId, ProjectDto.UpdateRequest request) {
		// 1. 프로젝트 및 멤버 조회
		Project project = findProjectById(projectId);
		ProjectMember member = findProjectMember(projectId, currentUserId);

		// 2. 관리자 권한(OWNER/ADMIN) 검증
		if (!member.isManager()) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		// 3. 프로젝트 정보 수정
		project.update(request.name(), request.description());
		return ProjectDto.Response.from(project);
	}

	// 프로젝트 삭제 (OWNER 전용)
	@Transactional
	public void deleteProject(Long currentUserId, Long projectId) {
		// 1. 프로젝트 및 멤버 조회
		Project project = findProjectById(projectId);
		ProjectMember member = findProjectMember(projectId, currentUserId);

		// 2. OWNER 권한 검증
		if (!member.isOwner()) {
			throw new BusinessException(ErrorCode.OWNER_REQUIRED);
		}

		// 3. FK 제약 방지를 위해 자식 데이터(Task, Member) 먼저 일괄 삭제
		taskRepository.deleteAllByProjectId(projectId);
		projectMemberRepository.deleteAllByProjectId(projectId);

		// 4. 프로젝트 본체 삭제
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
