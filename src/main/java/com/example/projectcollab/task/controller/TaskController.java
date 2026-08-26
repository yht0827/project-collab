package com.example.projectcollab.task.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "04. Task", description = "작업(할 일) 관리 API")
@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@Operation(summary = "작업 생성 (프로젝트 멤버 전용)")
	@PostMapping
	public ResponseEntity<ApiResponse<TaskDto.Response>> createTask(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@Valid @RequestBody TaskDto.CreateRequest request
	) {
		return ApiResponse.toCreated(taskService.createTask(currentUserId, projectId, request));
	}

	@Operation(summary = "작업 목록 조회 (상태 필터 + 라벨 필터 + 키워드 검색 + 페이징)")
	@GetMapping
	public ResponseEntity<ApiResponse<Page<TaskDto.Response>>> getTasks(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@ParameterObject TaskDto.SearchRequest condition,
		@ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponse.toOk(taskService.getTasks(currentUserId, projectId, condition, pageable));
	}

	@Operation(summary = "작업 단건 상세 조회")
	@GetMapping("/{taskId}")
	public ResponseEntity<ApiResponse<TaskDto.Response>> getTask(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@PathVariable Long taskId
	) {
		return ApiResponse.toOk(taskService.getTask(currentUserId, projectId, taskId));
	}

	@Operation(summary = "작업 수정 (담당자 본인 또는 OWNER, ADMIN / 동시 수정 시 409 Conflict)")
	@PutMapping("/{taskId}")
	public ResponseEntity<ApiResponse<TaskDto.Response>> updateTask(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@PathVariable Long taskId,
		@Valid @RequestBody TaskDto.UpdateRequest request
	) {
		return ApiResponse.toOk(taskService.updateTask(currentUserId, projectId, taskId, request));
	}

	@Operation(summary = "작업 삭제 (담당자 본인 또는 OWNER, ADMIN)")
	@DeleteMapping("/{taskId}")
	public ResponseEntity<ApiResponse<Void>> deleteTask(
		@CurrentUserId Long currentUserId,
		@PathVariable Long projectId,
		@PathVariable Long taskId
	) {
		taskService.deleteTask(currentUserId, projectId, taskId);
		return ApiResponse.toOk();
	}
}
