package com.example.projectcollab.project.dto;

import java.time.LocalDateTime;

import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectDto {

	@Schema(description = "프로젝트 생성 요청 DTO")
	public record CreateRequest(
		@NotBlank(message = "프로젝트 이름은 필수 입력값입니다.")
		@Size(max = Project.MAX_NAME_LENGTH, message = "프로젝트 이름은 최대 " + Project.MAX_NAME_LENGTH + "자까지 입력 가능합니다.")
		@Schema(description = "프로젝트 이름", example = "협업 플랫폼 서비스")
		String name,

		@Schema(description = "프로젝트 설명", example = "2026년 하반기 신규 협업 플랫폼 프로젝트")
		String description
	) {
	}

	@Schema(description = "프로젝트 정보 수정 요청 DTO")
	public record UpdateRequest(
		@NotBlank(message = "프로젝트 이름은 필수 입력값입니다.")
		@Size(max = Project.MAX_NAME_LENGTH, message = "프로젝트 이름은 최대 " + Project.MAX_NAME_LENGTH + "자까지 입력 가능합니다.")
		@Schema(description = "프로젝트 이름", example = "수정된 프로젝트 이름")
		String name,

		@Schema(description = "프로젝트 설명", example = "수정된 프로젝트 설명")
		String description
	) {
	}

	@Schema(description = "프로젝트 기본 응답 DTO")
	public record Response(
		@Schema(description = "프로젝트 ID", example = "1")
		Long id,

		@Schema(description = "프로젝트 이름", example = "협업 플랫폼 서비스")
		String name,

		@Schema(description = "프로젝트 설명", example = "신규 협업 플랫폼 프로젝트")
		String description,

		@Schema(description = "프로젝트 생성 일시")
		LocalDateTime createdAt,

		@Schema(description = "프로젝트 최종 수정 일시")
		LocalDateTime updatedAt
	) {
		public static Response from(Project project) {
			return new Response(
				project.getId(),
				project.getName(),
				project.getDescription(),
				project.getCreatedAt(),
				project.getUpdatedAt()
			);
		}
	}

	@Schema(description = "프로젝트 상세 조회 응답 DTO (내 역할 포함)")
	public record DetailResponse(
		@Schema(description = "프로젝트 ID", example = "1")
		Long id,

		@Schema(description = "프로젝트 이름", example = "협업 플랫폼 서비스")
		String name,

		@Schema(description = "프로젝트 설명", example = "신규 협업 플랫폼 프로젝트")
		String description,

		@Schema(description = "현재 조회자의 프로젝트 내 역할 (OWNER, ADMIN, MEMBER)", example = "OWNER")
		ProjectRole myRole,

		@Schema(description = "프로젝트 생성 일시")
		LocalDateTime createdAt,

		@Schema(description = "프로젝트 최종 수정 일시")
		LocalDateTime updatedAt
	) {
		public static DetailResponse of(Project project, ProjectRole myRole) {
			return new DetailResponse(
				project.getId(),
				project.getName(),
				project.getDescription(),
				myRole,
				project.getCreatedAt(),
				project.getUpdatedAt()
			);
		}
	}
}
