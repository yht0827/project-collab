package com.example.projectcollab.label.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectcollab.common.resolver.CurrentUserId;
import com.example.projectcollab.common.response.ApiResponse;
import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.label.service.LabelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Label", description = "프로젝트 라벨 관리 API")
@RestController
@RequestMapping("/api/v1/projects/{projectId}/labels")
@RequiredArgsConstructor
public class LabelController {

	private final LabelService labelService;

	@Operation(summary = "프로젝트 라벨 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<LabelDto.Response>>> getProjectLabels(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId
	) {
		return ApiResponse.toOk(labelService.getProjectLabels(currentUserId, projectId));
	}

	@Operation(summary = "신규 라벨 생성")
	@PostMapping
	public ResponseEntity<ApiResponse<LabelDto.Response>> createLabel(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@Valid @RequestBody LabelDto.CreateRequest request
	) {
		return ApiResponse.toCreated(labelService.createLabel(currentUserId, projectId, request));
	}

	@Operation(summary = "라벨 삭제 (OWNER, ADMIN 전용)")
	@DeleteMapping("/{labelId}")
	public ResponseEntity<ApiResponse<Void>> deleteLabel(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@PathVariable Long labelId
	) {
		labelService.deleteLabel(currentUserId, projectId, labelId);
		return ApiResponse.toOk();
	}
}
