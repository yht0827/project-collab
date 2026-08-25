package com.example.projectcollab.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.dto.ProjectMemberDto;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.entity.ProjectRole;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {

	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final UserService userService;

	// 멤버 목록 조회
	public List<ProjectMemberDto.Response> getMembers(Long currentUserId, Long projectId) {
		// 1. 요청자 멤버십 검증 (비멤버 시 403)
		validateMembership(projectId, currentUserId);

		// 2. 프로젝트 멤버 목록 조회 (Fetch Join) 및 DTO 변환
		return projectMemberRepository.findAllByProjectIdWithUser(projectId).stream()
			.map(ProjectMemberDto.Response::from)
			.toList();
	}

	// 멤버 추가 (OWNER, ADMIN 전용)
	@Transactional
	public ProjectMemberDto.Response addMember(Long currentUserId, Long projectId,
		ProjectMemberDto.AddRequest request) {
		// 1. 프로젝트 및 요청자 관리자 권한(OWNER/ADMIN) 검증
		Project project = findProjectById(projectId);
		validateManagerPermission(findMember(projectId, currentUserId));

		// 2. 추가할 대상 사용자 조회 및 중복 참여 검증
		User targetUser = userService.findUserById(request.userId());
		if (projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUser.getId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_PROJECT_MEMBER);
		}

		// 3. 멤버 생성 및 저장
		ProjectMember savedMember = projectMemberRepository.save(
			ProjectMember.createWithRole(project, targetUser, request.role())
		);
		return ProjectMemberDto.Response.from(savedMember);
	}

	// 멤버 역할 변경 (OWNER, ADMIN 전용, 마지막 OWNER 강등 방지)
	@Transactional
	public ProjectMemberDto.Response updateMemberRole(Long currentUserId, Long projectId, Long targetUserId,
		ProjectMemberDto.RoleUpdateRequest request) {
		// 1. 요청자 관리자 권한 검증 및 대상 멤버 조회
		validateManagerPermission(findMember(projectId, currentUserId));
		ProjectMember targetMember = findMember(projectId, targetUserId);

		// 2. 마지막 OWNER를 다른 역할로 강등 시도 시 차단
		if (request.role() != ProjectRole.OWNER) {
			validateNotLastOwner(projectId, targetMember);
		}

		// 3. 역할 변경
		targetMember.updateRole(request.role());
		return ProjectMemberDto.Response.from(targetMember);
	}

	// 멤버 추방 또는 본인 탈퇴 (마지막 OWNER 탈퇴 방지)
	@Transactional
	public void removeMember(Long currentUserId, Long projectId, Long targetUserId) {
		// 1. 요청자 및 대상 멤버 조회
		ProjectMember currentMember = findMember(projectId, currentUserId);
		ProjectMember targetMember = findMember(projectId, targetUserId);

		// 2. 권한 검증 (관리자이거나 본인 탈퇴인 경우만 허용)
		boolean isSelfLeaving = currentUserId.equals(targetUserId);
		if (!currentMember.isManager() && !isSelfLeaving) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		// 3. 유일한 OWNER인 경우 탈퇴/제거 차단
		validateNotLastOwner(projectId, targetMember);

		// 4. 멤버 삭제
		projectMemberRepository.delete(targetMember);
	}

	// 프로젝트 생성자 OWNER 등록
	@Transactional
	public ProjectMember registerOwner(Project project, User user) {
		return projectMemberRepository.save(ProjectMember.createOwner(project, user));
	}

	public ProjectMember findMember(Long projectId, Long userId) {
		return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
	}

	private void validateNotLastOwner(Long projectId, ProjectMember targetMember) {
		if (targetMember.isOwner()
			&& projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER) <= 1) {
			throw new BusinessException(ErrorCode.CANNOT_REMOVE_LAST_OWNER);
		}
	}

	private void validateManagerPermission(ProjectMember member) {
		if (!member.isManager()) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateMembership(Long projectId, Long userId) {
		if (!projectRepository.existsById(projectId)) {
			throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
		}
		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
			throw new BusinessException(ErrorCode.PROJECT_MEMBER_REQUIRED);
		}
	}

	private Project findProjectById(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
	}
}
