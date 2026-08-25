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
	 * 단순 동적 쿼리를 @Query JPQL로 처리하여 별도 의존성 없이 간결하게 구현
	 */
	@Query("SELECT t FROM Task t " +
		"LEFT JOIN FETCH t.assignee " +
		"WHERE t.project.id = :projectId " +
		"AND (:status IS NULL OR t.status = :status) " +
		"AND (:keyword IS NULL OR (t.title LIKE %:keyword% OR t.description LIKE %:keyword%))")
	Page<Task> searchTasks(
		@Param("projectId") Long projectId,
		@Param("status") TaskStatus status,
		@Param("keyword") String keyword,
		Pageable pageable
	);
}
