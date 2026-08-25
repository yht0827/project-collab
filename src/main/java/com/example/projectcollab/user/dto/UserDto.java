package com.example.projectcollab.user.dto;

import java.time.LocalDateTime;

import com.example.projectcollab.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {

	@Schema(description = "사용자 등록 요청 DTO")
	public record CreateRequest(
		@NotBlank(message = "이름은 필수 입력값입니다.")
		@Size(max = User.MAX_NAME_LENGTH, message = "이름은 최대 " + User.MAX_NAME_LENGTH + "자까지 입력 가능합니다.")
		@Schema(description = "사용자 이름", example = "홍길동")
		String name
	) {
	}

	@Schema(description = "사용자 응답 DTO")
	public record Response(
		@Schema(description = "사용자 ID", example = "1")
		Long id,

		@Schema(description = "사용자 이름", example = "홍길동")
		String name,

		@Schema(description = "계정 생성 일시")
		LocalDateTime createdAt,

		@Schema(description = "계정 수정 일시")
		LocalDateTime updatedAt
	) {
		public static Response from(User user) {
			return new Response(
				user.getId(),
				user.getName(),
				user.getCreatedAt(),
				user.getUpdatedAt()
			);
		}
	}
}
