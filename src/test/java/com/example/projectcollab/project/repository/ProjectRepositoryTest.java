package com.example.projectcollab.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.projectcollab.common.config.JpaConfig;
import com.example.projectcollab.project.entity.Project;

@DataJpaTest
@Import(JpaConfig.class)
class ProjectRepositoryTest {

	@Autowired
	private ProjectRepository projectRepository;

	@Nested
	@DisplayName("프로젝트 저장 및 Auditing 검증")
	class SaveAndAuditing {

		@Test
		@DisplayName("성공: 프로젝트를 저장하면 ID가 생성되고 createdAt, updatedAt이 자동으로 기록된다")
		void saveProjectWithAuditing() {
			// given
			Project project = Project.createProject("신규 협업 프로젝트", "프로젝트 상세 설명");

			// when
			Project savedProject = projectRepository.save(project);

			// then
			assertThat(savedProject.getId()).isNotNull();
			assertThat(savedProject.getName()).isEqualTo("신규 협업 프로젝트");
			assertThat(savedProject.getDescription()).isEqualTo("프로젝트 상세 설명");
			assertThat(savedProject.getCreatedAt()).isNotNull();
			assertThat(savedProject.getUpdatedAt()).isNotNull();
		}
	}

	@Nested
	@DisplayName("프로젝트 조회 및 삭제 검증")
	class FindAndDelete {

		@Test
		@DisplayName("성공: 저장된 프로젝트를 조회하고 삭제한다")
		void findAndDeleteSuccess() {
			// given
			Project project = projectRepository.save(Project.createProject("삭제 대상 프로젝트", "설명"));
			Long projectId = project.getId();

			// when
			Optional<Project> foundProject = projectRepository.findById(projectId);
			assertThat(foundProject).isPresent();

			projectRepository.delete(foundProject.get());
			Optional<Project> afterDelete = projectRepository.findById(projectId);

			// then
			assertThat(afterDelete).isEmpty();
		}
	}
}
