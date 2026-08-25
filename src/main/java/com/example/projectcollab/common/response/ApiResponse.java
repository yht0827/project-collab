package com.example.projectcollab.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, null, data);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}

	public static ApiResponse<Void> success() {
		return new ApiResponse<>(true, null, null);
	}

	public static ApiResponse<Void> success(String message) {
		return new ApiResponse<>(true, message, null);
	}

	// HTTP 200 OK 응답 (데이터 포함)
	public static <T> ResponseEntity<ApiResponse<T>> toOk(T data) {
		return ResponseEntity.ok(ApiResponse.success(data));
	}

	// HTTP 200 OK 응답 (데이터 없음)
	public static ResponseEntity<ApiResponse<Void>> toOk() {
		return ResponseEntity.ok(ApiResponse.success());
	}

	// HTTP 201 CREATED 응답 (데이터 포함)
	public static <T> ResponseEntity<ApiResponse<T>> toCreated(T data) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
	}
}
