package com.example.projectcollab.task.repository;

import org.springframework.data.jpa.domain.Specification;

import com.example.projectcollab.label.entity.TaskLabel;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.entity.TaskStatus;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskSpecification {

	/**
	 * 프로젝트 ID 및 검색 조건(상태, 라벨, 키워드)을 결합한 통합 Specification 생성
	 */
	public static Specification<Task> searchBy(Long projectId, TaskDto.SearchRequest condition) {
		Specification<Task> spec = Specification.where(equalProjectId(projectId));

		if (condition == null) {
			return spec;
		}

		return spec
			.and(equalStatus(condition.status()))
			.and(hasLabelId(condition.labelId()))
			.and(containsKeyword(condition.keyword()));
	}

	public static Specification<Task> equalProjectId(Long projectId) {
		return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
	}

	public static Specification<Task> equalStatus(TaskStatus status) {
		return (root, query, cb) -> {
			if (status == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get("status"), status);
		};
	}

	public static Specification<Task> hasLabelId(Long labelId) {
		return (root, query, cb) -> {
			if (labelId == null) {
				return cb.conjunction();
			}
			Subquery<Long> subquery = query.subquery(Long.class);
			Root<TaskLabel> taskLabelRoot = subquery.from(TaskLabel.class);
			subquery.select(cb.literal(1L))
				.where(
					cb.equal(taskLabelRoot.get("task"), root),
					cb.equal(taskLabelRoot.get("label").get("id"), labelId)
				);
			return cb.exists(subquery);
		};
	}

	public static Specification<Task> containsKeyword(String keyword) {
		return (root, query, cb) -> {
			if (keyword == null || keyword.trim().isEmpty()) {
				return cb.conjunction();
			}
			String pattern = "%" + keyword.trim().toLowerCase() + "%";
			Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
			Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
			return cb.or(titleMatch, descMatch);
		};
	}
}
