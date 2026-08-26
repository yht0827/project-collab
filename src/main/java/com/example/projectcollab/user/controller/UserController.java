package com.example.projectcollab.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectcollab.common.response.ApiResponse;
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "01. User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "신규 사용자 등록")
	@PostMapping
	public ResponseEntity<ApiResponse<UserDto.Response>> createUser(@Valid @RequestBody UserDto.CreateRequest request) {
		return ApiResponse.toCreated(userService.createUser(request));
	}

	@Operation(summary = "전체 사용자 목록 조회")
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserDto.Response>>> getAllUsers() {
		return ApiResponse.toOk(userService.getAllUsers());
	}

	@Operation(summary = "사용자 단건 상세 조회")
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<UserDto.Response>> getUser(@PathVariable Long userId) {
		return ApiResponse.toOk(userService.getUser(userId));
	}
}
