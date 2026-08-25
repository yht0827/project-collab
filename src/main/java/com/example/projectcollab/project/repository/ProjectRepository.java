package com.example.projectcollab.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.projectcollab.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
