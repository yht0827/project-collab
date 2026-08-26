package com.example.projectcollab.task.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.projectcollab.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Task t WHERE t.project.id = :projectId")
	void deleteAllByProjectId(@Param("projectId") Long projectId);

	/**
	 * Specification 기반 동적 검색
	 */
	@Override
	@EntityGraph(attributePaths = {"assignee"})
	Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}
