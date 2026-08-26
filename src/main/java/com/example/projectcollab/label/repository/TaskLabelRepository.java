package com.example.projectcollab.label.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.projectcollab.label.entity.TaskLabel;

public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {

	@Modifying
	@Query("DELETE FROM TaskLabel tl WHERE tl.task.id = :taskId")
	void deleteByTaskId(@Param("taskId") Long taskId);

	@Modifying
	@Query("DELETE FROM TaskLabel tl WHERE tl.label.id = :labelId")
	void deleteByLabelId(@Param("labelId") Long labelId);
}
