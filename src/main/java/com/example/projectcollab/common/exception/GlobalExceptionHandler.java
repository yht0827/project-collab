package com.example.projectcollab.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.projectcollab.common.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 커스텀 비즈니스 예외 처리
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("BusinessException: code={}, message={}", errorCode.name(), e.getMessage());

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponse.of(errorCode));
	}

	// @Valid 파라미터 유효성 검증 실패 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult().getFieldError();
		String errorMessage = (fieldError != null)
			? fieldError.getDefaultMessage()
			: ErrorCode.INVALID_INPUT_VALUE.getMessage();

		log.warn("MethodArgumentNotValidException: {}", errorMessage);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errorMessage));
	}

	// IllegalArgumentException 처리
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		log.warn("IllegalArgumentException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage()));
	}

	// 낙관적 락 충돌 (@Version 동시 수정 실패) 처리
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ErrorResponse> handleOptimisticLockException(ObjectOptimisticLockingFailureException e) {
		log.warn("ObjectOptimisticLockingFailureException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.CONFLICT)
			.body(ErrorResponse.of(ErrorCode.CONCURRENT_MODIFICATION));
	}

	// 기타 예상하지 못한 서버 내부 오류 (500)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unhandled Exception: ", e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}