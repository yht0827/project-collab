package com.example.projectcollab.label.dto;

import com.example.projectcollab.label.entity.Label;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LabelDto {

	@Schema(description = "라벨 생성 요청 DTO")
	public record CreateRequest(
		@NotBlank(message = "라벨 이름은 필수 입력값입니다.")
		@Size(max = Label.MAX_NAME_LENGTH, message = "라벨 이름은 최대 " + Label.MAX_NAME_LENGTH + "자까지 입력 가능합니다.")
		@Schema(description = "라벨 이름", example = "Bug")
		String name,

		@Schema(description = "라벨 테마 색상 (#HEX)", example = "#ef4444")
		String color
	) {
	}

	@Schema(description = "라벨 응답 DTO")
	public record Response(
		@Schema(description = "라벨 ID", example = "1")
		Long id,

		@Schema(description = "소속 프로젝트 ID", example = "1")
		Long projectId,

		@Schema(description = "라벨 이름", example = "Bug")
		String name,

		@Schema(description = "라벨 색상", example = "#ef4444")
		String color
	) {
		public static Response from(Label label) {
			return new Response(
				label.getId(),
				label.getProject().getId(),
				label.getName(),
				label.getColor()
			);
		}
	}
}
