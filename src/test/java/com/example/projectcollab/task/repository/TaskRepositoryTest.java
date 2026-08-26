package com.example.projectcollab.task.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.projectcollab.common.config.JpaConfig;
import com.example.projectcollab.label.entity.Label;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.entity.TaskStatus;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.repository.UserRepository;

@DataJpaTest
@Import(JpaConfig.class)
class TaskRepositoryTest {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestEntityManager em;

	private Project project;
	private User assignee;
	private Task task1;
	private Task task2;
	private Task task3;
	private Label backendLabel;

	@BeforeEach
	void setUp() {
		project = projectRepository.save(Project.createProject("테스트 프로젝트", "설명"));
		assignee = userRepository.save(User.createUser("담당자"));

		backendLabel = Label.createLabel(project, "Backend", "#10b981");
		em.persist(backendLabel);

		task1 = Task.createTask(project, assignee, "Spring Boot 백엔드 개발", "REST API 및 동시성 제어");
		task1.updateLabels(List.of(backendLabel));

		task2 = Task.createTask(project, null, "React 프론트엔드 개발", "화면 UI 및 컴포넌트 설계");
		task3 = Task.createTask(project, assignee, "배포 및 인프라 구축", "AWS 환경 세팅");

		task2.updateStatus(TaskStatus.IN_PROGRESS);
		task3.updateStatus(TaskStatus.DONE);

		taskRepository.save(task1);
		taskRepository.save(task2);
		taskRepository.save(task3);

		em.flush();
		em.clear();
	}

	@Nested
	@DisplayName("TaskSpecification 동적 검색 및 페이징 검증")
	class SearchTasksQuery {

		@Test
		@DisplayName("성공: 조건 없이 전체 작업을 페이징 조회한다")
		void searchWithoutConditions() {
			Pageable pageable = PageRequest.of(0, 10);
			var spec = Specification.where(TaskSpecification.equalProjectId(project.getId()));

			Page<Task> result = taskRepository.findAll(spec, pageable);

			assertThat(result.getTotalElements()).isEqualTo(3);
			assertThat(result.getContent()).hasSize(3);
		}

		@Test
		@DisplayName("성공: status 필터를 적용하여 IN_PROGRESS 상태의 작업만 조회한다")
		void searchByStatus() {
			Pageable pageable = PageRequest.of(0, 10);
			var spec = Specification.where(TaskSpecification.equalProjectId(project.getId()))
				.and(TaskSpecification.equalStatus(TaskStatus.IN_PROGRESS));

			Page<Task> result = taskRepository.findAll(spec, pageable);

			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).getTitle()).isEqualTo("React 프론트엔드 개발");
		}

		@Test
		@DisplayName("성공: labelId 필터를 적용하여 해당 라벨이 달린 작업만 조회한다")
		void searchByLabelId() {
			Pageable pageable = PageRequest.of(0, 10);
			var spec = Specification.where(TaskSpecification.equalProjectId(project.getId()))
				.and(TaskSpecification.hasLabelId(backendLabel.getId()));

			Page<Task> result = taskRepository.findAll(spec, pageable);

			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring Boot 백엔드 개발");
		}

		@Test
		@DisplayName("성공: keyword(제목 또는 설명)로 검색하여 매칭된 작업을 조회한다")
		void searchByKeyword() {
			Pageable pageable = PageRequest.of(0, 10);
			var spec = Specification.where(TaskSpecification.equalProjectId(project.getId()))
				.and(TaskSpecification.containsKeyword("Spring"));

			Page<Task> result = taskRepository.findAll(spec, pageable);

			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring Boot 백엔드 개발");
		}

		@Test
		@DisplayName("성공: status + keyword 복합 조건으로 검색한다")
		void searchByStatusAndKeyword() {
			Pageable pageable = PageRequest.of(0, 10);
			var spec = Specification.where(TaskSpecification.equalProjectId(project.getId()))
				.and(TaskSpecification.equalStatus(TaskStatus.DONE))
				.and(TaskSpecification.containsKeyword("AWS"));

			Page<Task> result = taskRepository.findAll(spec, pageable);

			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).getTitle()).isEqualTo("배포 및 인프라 구축");
		}
	}

	@Nested
	@DisplayName("deleteAllByProjectId 벌크 삭제 검증")
	class DeleteAllByProjectId {

		@Test
		@DisplayName("성공: 프로젝트에 속한 모든 작업을 일괄 삭제한다")
		void deleteAllTasksInProject() {
			taskRepository.deleteAllByProjectId(project.getId());

			var spec = Specification.where(TaskSpecification.equalProjectId(project.getId()));
			Page<Task> result = taskRepository.findAll(spec, PageRequest.of(0, 10));
			assertThat(result.getTotalElements()).isEqualTo(0);
		}
	}
}
