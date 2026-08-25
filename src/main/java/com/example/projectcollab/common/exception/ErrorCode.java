package com.example.projectcollab.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 400 BAD_REQUEST
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
	CANNOT_REMOVE_LAST_OWNER(HttpStatus.BAD_REQUEST, "프로젝트의 마지막 소유자는 탈퇴하거나 권한을 제거할 수 없습니다."),
	ASSIGNEE_NOT_PROJECT_MEMBER(HttpStatus.BAD_REQUEST, "프로젝트 멤버만 담당자로 지정할 수 있습니다."),

	// 401 UNAUTHORIZED
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "요청자 식별자(User ID)가 누락되었거나 유효하지 않습니다."),

	// 403 FORBIDDEN
	PROJECT_MEMBER_REQUIRED(HttpStatus.FORBIDDEN, "프로젝트 멤버만 접근할 수 있습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	OWNER_REQUIRED(HttpStatus.FORBIDDEN, "프로젝트 소유자 권한이 필요합니다."),

	// 404 NOT_FOUND
	PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
	TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 작업을 찾을 수 없습니다."),
	PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 프로젝트에 참여 중인 멤버를 찾을 수 없습니다."),

	// 409 CONFLICT
	DUPLICATE_PROJECT_MEMBER(HttpStatus.CONFLICT, "이미 등록된 프로젝트 멤버입니다."),
	CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "다른 사용자에 의해 수정되었습니다. 다시 시도해주세요."),

	// 500 INTERNAL_SERVER_ERROR
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
