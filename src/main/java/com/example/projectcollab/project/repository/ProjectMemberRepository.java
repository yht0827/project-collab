package com.example.projectcollab.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.entity.ProjectRole;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

	Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

	boolean existsByProjectIdAndUserId(Long projectId, Long userId);

	@Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user WHERE pm.project.id = :projectId")
	List<ProjectMember> findAllByProjectIdWithUser(@Param("projectId") Long projectId);

	long countByProjectIdAndRole(Long projectId, ProjectRole role);

	@Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.project WHERE pm.user.id = :userId")
	List<ProjectMember> findAllByUserIdWithProject(@Param("userId") Long userId);

	void deleteAllByProjectId(Long projectId);
}
