package com.example.projectcollab.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectcollab.common.resolver.CurrentUserId;
import com.example.projectcollab.common.response.ApiResponse;
import com.example.projectcollab.project.dto.ProjectMemberDto;
import com.example.projectcollab.project.service.ProjectMemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "03. Project Member", description = "프로젝트 멤버 관리 API")
@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

	private final ProjectMemberService projectMemberService;

	@Operation(summary = "프로젝트 멤버 목록 조회 (멤버 전용)")
	@GetMapping
	public ResponseEntity<ApiResponse<List<ProjectMemberDto.Response>>> getMembers(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId
	) {
		return ApiResponse.toOk(projectMemberService.getMembers(currentUserId, projectId));
	}

	@Operation(summary = "프로젝트 멤버 추가/초대 (OWNER, ADMIN 전용)")
	@PostMapping
	public ResponseEntity<ApiResponse<ProjectMemberDto.Response>> addMember(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@Valid @RequestBody ProjectMemberDto.AddRequest request
	) {
		return ApiResponse.toCreated(projectMemberService.addMember(currentUserId, projectId, request));
	}

	@Operation(summary = "프로젝트 멤버 역할 변경 (OWNER, ADMIN 전용, 마지막 OWNER 강등 방지)")
	@PutMapping("/{userId}")
	public ResponseEntity<ApiResponse<ProjectMemberDto.Response>> updateMemberRole(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@PathVariable Long userId,
		@Valid @RequestBody ProjectMemberDto.RoleUpdateRequest request
	) {
		return ApiResponse.toOk(projectMemberService.updateMemberRole(currentUserId, projectId, userId, request));
	}

	@Operation(summary = "프로젝트 멤버 추방 또는 탈퇴 (관리자 또는 본인 전용, 마지막 OWNER 탈퇴 방지)")
	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse<Void>> removeMember(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@PathVariable Long userId
	) {
		projectMemberService.removeMember(currentUserId, projectId, userId);
		return ApiResponse.toOk();
	}
}
