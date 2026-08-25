package com.example.projectcollab.project.dto;

import java.time.LocalDateTime;

import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.entity.ProjectRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class ProjectMemberDto {

	@Schema(description = "프로젝트 멤버 추가 요청 DTO")
	public record AddRequest(
		@NotNull(message = "추가할 사용자 ID는 필수값입니다.")
		@Schema(description = "추가할 대상 사용자 ID", example = "2")
		Long userId,

		@NotNull(message = "부여할 역할은 필수값입니다.")
		@Schema(description = "부여할 역할 (OWNER, ADMIN, MEMBER)", example = "MEMBER")
		ProjectRole role
	) {
	}

	@Schema(description = "프로젝트 멤버 역할 변경 요청 DTO")
	public record RoleUpdateRequest(
		@NotNull(message = "변경할 역할은 필수값입니다.")
		@Schema(description = "새로운 역할 (OWNER, ADMIN, MEMBER)", example = "ADMIN")
		ProjectRole role
	) {
	}

	@Schema(description = "프로젝트 멤버 정보 응답 DTO")
	public record Response(
		@Schema(description = "멤버십 ID", example = "1")
		Long id,

		@Schema(description = "사용자 ID", example = "2")
		Long userId,

		@Schema(description = "사용자 이름", example = "홍길동")
		String userName,

		@Schema(description = "프로젝트 내 역할 (OWNER, ADMIN, MEMBER)", example = "MEMBER")
		ProjectRole role,

		@Schema(description = "프로젝트 참여 일시")
		LocalDateTime joinedAt
	) {
		public static Response from(ProjectMember projectMember) {
			return new Response(
				projectMember.getId(),
				projectMember.getUser().getId(),
				projectMember.getUser().getName(),
				projectMember.getRole(),
				projectMember.getCreatedAt()
			);
		}
	}
}
