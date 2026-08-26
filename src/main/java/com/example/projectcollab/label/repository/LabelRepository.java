package com.example.projectcollab.label.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.projectcollab.label.entity.Label;

public interface LabelRepository extends JpaRepository<Label, Long> {

	List<Label> findByProjectIdOrderByNameAsc(Long projectId);

	boolean existsByProjectIdAndName(Long projectId, String name);

	Optional<Label> findByIdAndProjectId(Long id, Long projectId);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM Label l WHERE l.project.id = :projectId")
	void deleteAllByProjectId(@Param("projectId") Long projectId);
}
