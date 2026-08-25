package com.example.projectcollab.common.response;

import java.time.LocalDateTime;

import com.example.projectcollab.common.exception.ErrorCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

	private final LocalDateTime timestamp;
	private final String code;
	private final String message;

	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.code(errorCode.name())
			.message(errorCode.getMessage())
			.build();
	}

	public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
		return ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.code(errorCode.name())
			.message(customMessage)
			.build();
	}
}
