package com.example.projectcollab.task.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Task t WHERE t.project.id = :projectId")
	void deleteAllByProjectId(@Param("projectId") Long projectId);

	/**
	 * 프로젝트 내 작업 목록 검색 (상태 필터 + 키워드 검색 + 페이징)
	 * - 데이터 조회: LEFT JOIN FETCH로 N+1 문제 해결
	 * - countQuery 분리: 불필요한 조인을 제거한 초경량 카운트 쿼리로 대용량 페이징 성능 최적화
	 */
	@Query(
		value = "SELECT t FROM Task t " +
			"LEFT JOIN FETCH t.assignee " +
			"WHERE t.project.id = :projectId " +
			"AND (:status IS NULL OR t.status = :status) " +
			"AND (:keyword IS NULL OR (t.title LIKE %:keyword% OR t.description LIKE %:keyword%))",
		countQuery = "SELECT COUNT(t) FROM Task t " +
			"WHERE t.project.id = :projectId " +
			"AND (:status IS NULL OR t.status = :status) " +
			"AND (:keyword IS NULL OR (t.title LIKE %:keyword% OR t.description LIKE %:keyword%))"
	)
	Page<Task> searchTasks(
		@Param("projectId") Long projectId,
		@Param("status") TaskStatus status,
		@Param("keyword") String keyword,
		Pageable pageable
	);
}
