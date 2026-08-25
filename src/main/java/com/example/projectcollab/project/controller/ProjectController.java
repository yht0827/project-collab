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
import com.example.projectcollab.project.dto.ProjectDto;
import com.example.projectcollab.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "02. Project", description = "프로젝트 관리 API")
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService projectService;

	@Operation(summary = "프로젝트 생성 (생성자 OWNER 자동 등록)")
	@PostMapping
	public ResponseEntity<ApiResponse<ProjectDto.Response>> createProject(
		@CurrentUserId Long currentUserId,
		@Valid @RequestBody ProjectDto.CreateRequest request
	) {
		return ApiResponse.toCreated(projectService.createProject(currentUserId, request));
	}

	@Operation(summary = "내 참여 프로젝트 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<ProjectDto.Response>>> getMyProjects(
		@CurrentUserId Long currentUserId
	) {
		return ApiResponse.toOk(projectService.getMyProjects(currentUserId));
	}

	@Operation(summary = "프로젝트 상세 조회 (멤버 전용, 내 역할 포함)")
	@GetMapping("/{projectId}")
	public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> getProjectDetail(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId
	) {
		return ApiResponse.toOk(projectService.getProjectDetail(currentUserId, projectId));
	}

	@Operation(summary = "프로젝트 정보 수정 (OWNER, ADMIN 전용)")
	@PutMapping("/{projectId}")
	public ResponseEntity<ApiResponse<ProjectDto.Response>> updateProject(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@Valid @RequestBody ProjectDto.UpdateRequest request
	) {
		return ApiResponse.toOk(projectService.updateProject(currentUserId, projectId, request));
	}

	@Operation(summary = "프로젝트 삭제 (OWNER 전용)")
	@DeleteMapping("/{projectId}")
	public ResponseEntity<ApiResponse<Void>> deleteProject(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId
	) {
		projectService.deleteProject(currentUserId, projectId);
		return ApiResponse.toOk();
	}
}
