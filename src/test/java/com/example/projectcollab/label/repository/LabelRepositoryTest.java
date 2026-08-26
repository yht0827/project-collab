package com.example.projectcollab.label.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.projectcollab.label.entity.Label;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.repository.ProjectRepository;

@DataJpaTest
class LabelRepositoryTest {

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	@DisplayName("프로젝트 ID로 라벨 목록 조회 시 이름 오름차순 정렬 확인")
	void findByProjectIdOrderByNameAsc() {
		Project project = projectRepository.save(Project.createProject("라벨테스트", "설명"));
		labelRepository.save(Label.createLabel(project, "Frontend", "#3b82f6"));
		labelRepository.save(Label.createLabel(project, "Backend", "#10b981"));

		List<Label> labels = labelRepository.findByProjectIdOrderByNameAsc(project.getId());

		assertThat(labels).hasSize(2);
		assertThat(labels.get(0).getName()).isEqualTo("Backend");
		assertThat(labels.get(1).getName()).isEqualTo("Frontend");
	}

	@Test
	@DisplayName("프로젝트 내 동일한 라벨 이름 중복 여부 확인")
	void existsByProjectIdAndName() {
		Project project = projectRepository.save(Project.createProject("라벨테스트2", "설명"));
		labelRepository.save(Label.createLabel(project, "Bug", "#ef4444"));

		assertThat(labelRepository.existsByProjectIdAndName(project.getId(), "Bug")).isTrue();
		assertThat(labelRepository.existsByProjectIdAndName(project.getId(), "Feature")).isFalse();
	}
}
