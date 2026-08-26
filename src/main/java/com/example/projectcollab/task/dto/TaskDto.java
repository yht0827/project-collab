package com.example.projectcollab.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskDto {

	@Schema(description = "작업 생성 요청 DTO")
	public record CreateRequest(
		@NotBlank(message = "작업 제목은 필수 입력값입니다.")
		@Size(max = Task.MAX_TITLE_LENGTH, message = "작업 제목은 최대 " + Task.MAX_TITLE_LENGTH + "자까지 입력 가능합니다.")
		@Schema(description = "작업 제목", example = "로그인 API 구현")
		String title,

		@Schema(description = "작업 상세 설명", example = "Spring Security와 JWT를 활용한 로그인 기능 개발")
		String description,

		@Schema(description = "담당자 사용자 ID (선택사항, 프로젝트 멤버만 지정 가능)", example = "2")
		Long assigneeId,

		@Schema(description = "작업 마감일 (선택사항)", example = "2026-08-30")
		LocalDate dueDate,

		@Schema(description = "적용할 라벨 ID 목록 (선택사항)", example = "[1, 2]")
		List<Long> labelIds
	) {
		public CreateRequest(String title, String description, Long assigneeId) {
			this(title, description, assigneeId, null, null);
		}
	}

	@Schema(description = "작업 수정 요청 DTO")
	public record UpdateRequest(
		@NotBlank(message = "작업 제목은 필수 입력값입니다.")
		@Size(max = Task.MAX_TITLE_LENGTH, message = "작업 제목은 최대 " + Task.MAX_TITLE_LENGTH + "자까지 입력 가능합니다.")
		@Schema(description = "수정할 작업 제목", example = "수정된 작업 제목")
		String title,

		@Schema(description = "수정할 작업 설명", example = "수정된 작업 상세 내용")
		String description,

		@Schema(description = "수정할 담당자 사용자 ID (선택사항)", example = "3")
		Long assigneeId,

		@NotNull(message = "작업 상태는 필수값입니다.")
		@Schema(description = "작업 진행 상태 (TODO, IN_PROGRESS, DONE)", example = "IN_PROGRESS")
		TaskStatus status,

		@Schema(description = "수정할 작업 마감일 (선택사항)", example = "2026-08-31")
		LocalDate dueDate,

		@Schema(description = "수정할 라벨 ID 목록 (선택사항)", example = "[1, 3]")
		List<Long> labelIds
	) {
		public UpdateRequest(String title, String description, Long assigneeId, TaskStatus status) {
			this(title, description, assigneeId, status, null, null);
		}
	}

	@Schema(description = "작업 목록 검색 조건 DTO")
	public record SearchRequest(
		@Schema(description = "작업 상태 필터 (TODO, IN_PROGRESS, DONE)", example = "IN_PROGRESS")
		TaskStatus status,

		@Schema(description = "라벨 ID 필터 (선택사항)", example = "1")
		Long labelId,

		@Schema(description = "검색 키워드 (제목 또는 설명)", example = "Spring")
		String keyword
	) {
		public static SearchRequest of(TaskStatus status, String keyword) {
			return new SearchRequest(status, null, keyword);
		}

		public static SearchRequest empty() {
			return new SearchRequest(null, null, null);
		}
	}

	@Schema(description = "작업 상세 정보 응답 DTO")
	public record Response(
		@Schema(description = "작업 ID", example = "1")
		Long id,

		@Schema(description = "소속 프로젝트 ID", example = "1")
		Long projectId,

		@Schema(description = "작업 제목", example = "로그인 API 구현")
		String title,

		@Schema(description = "작업 상세 설명", example = "Spring Security와 JWT를 활용한 로그인 기능 개발")
		String description,

		@Schema(description = "작업 진행 상태 (TODO, IN_PROGRESS, DONE)", example = "TODO")
		TaskStatus status,

		@Schema(description = "담당자 사용자 ID", example = "2")
		Long assigneeId,

		@Schema(description = "담당자 사용자 이름", example = "홍길동")
		String assigneeName,

		@Schema(description = "작업 마감일", example = "2026-08-30")
		LocalDate dueDate,

		@Schema(description = "적용된 라벨 목록")
		List<LabelDto.Response> labels,

		@Schema(description = "동시성 제어용 낙관적 락 버전 번호", example = "0")
		Long version,

		@Schema(description = "작업 생성 일시")
		LocalDateTime createdAt,

		@Schema(description = "작업 최종 수정 일시")
		LocalDateTime updatedAt
	) {
		public static Response from(Task task) {
			return from(task, Collections.emptyList());
		}

		public static Response from(Task task, List<LabelDto.Response> labels) {
			Long assigneeId = (task.getAssignee() != null) ? task.getAssignee().getId() : null;
			String assigneeName = (task.getAssignee() != null) ? task.getAssignee().getName() : null;

			return new Response(
				task.getId(),
				task.getProject().getId(),
				task.getTitle(),
				task.getDescription(),
				task.getStatus(),
				assigneeId,
				assigneeName,
				task.getDueDate(),
				labels != null ? labels : Collections.emptyList(),
				task.getVersion(),
				task.getCreatedAt(),
				task.getUpdatedAt()
			);
		}
	}
}
