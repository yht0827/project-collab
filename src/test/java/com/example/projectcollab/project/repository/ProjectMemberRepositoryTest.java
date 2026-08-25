package com.example.projectcollab.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.example.projectcollab.common.config.JpaConfig;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.entity.ProjectRole;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.repository.UserRepository;

@DataJpaTest
@Import(JpaConfig.class)
class ProjectMemberRepositoryTest {

	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestEntityManager em;

	private User user1;
	private User user2;
	private Project project1;

	@BeforeEach
	void setUp() {
		user1 = userRepository.save(User.createUser("홍길동"));
		user2 = userRepository.save(User.createUser("김철수"));
		project1 = projectRepository.save(Project.createProject("프로젝트1", "설명1"));
	}

	@Nested
	@DisplayName("Fetch Join 조회 쿼리 검증")
	class FetchJoinQueries {

		@Test
		@DisplayName("성공: findAllByUserIdWithProject는 Project를 Fetch Join하여 N+1 없이 조회한다")
		void findAllByUserIdWithProject() {
			// given
			projectMemberRepository.save(ProjectMember.createOwner(project1, user1));
			em.flush();
			em.clear();

			// when
			List<ProjectMember> memberships = projectMemberRepository.findAllByUserIdWithProject(user1.getId());

			// then
			assertThat(memberships).hasSize(1);
			assertThat(memberships.get(0).getProject().getName()).isEqualTo("프로젝트1");
			assertThat(memberships.get(0).getRole()).isEqualTo(ProjectRole.OWNER);
		}

		@Test
		@DisplayName("성공: findAllByProjectIdWithUser는 User를 Fetch Join하여 N+1 없이 조회한다")
		void findAllByProjectIdWithUser() {
			// given
			projectMemberRepository.save(ProjectMember.createOwner(project1, user1));
			projectMemberRepository.save(ProjectMember.createWithRole(project1, user2, ProjectRole.ADMIN));
			em.flush();
			em.clear();

			// when
			List<ProjectMember> members = projectMemberRepository.findAllByProjectIdWithUser(project1.getId());

			// then
			assertThat(members).hasSize(2);
			assertThat(members.get(0).getUser().getName()).isNotNull();
		}
	}

	@Nested
	@DisplayName("단건 조회 및 조건 검증")
	class ConditionQueries {

		@Test
		@DisplayName("성공: findByProjectIdAndUserId로 특정 사용자의 멤버십을 조회한다")
		void findByProjectIdAndUserId() {
			// given
			projectMemberRepository.save(ProjectMember.createOwner(project1, user1));

			// when
			Optional<ProjectMember> member = projectMemberRepository.findByProjectIdAndUserId(project1.getId(), user1.getId());

			// then
			assertThat(member).isPresent();
			assertThat(member.get().getRole()).isEqualTo(ProjectRole.OWNER);
		}

		@Test
		@DisplayName("성공: existsByProjectIdAndUserId로 참여 여부를 확인한다")
		void existsByProjectIdAndUserId() {
			// given
			projectMemberRepository.save(ProjectMember.createOwner(project1, user1));

			// then
			assertThat(projectMemberRepository.existsByProjectIdAndUserId(project1.getId(), user1.getId())).isTrue();
			assertThat(projectMemberRepository.existsByProjectIdAndUserId(project1.getId(), user2.getId())).isFalse();
		}

		@Test
		@DisplayName("성공: countByProjectIdAndRole로 특정 역할의 멤버 수를 카운트한다 (마지막 OWNER 보호용)")
		void countByProjectIdAndRole() {
			// given
			projectMemberRepository.save(ProjectMember.createOwner(project1, user1));
			projectMemberRepository.save(ProjectMember.createWithRole(project1, user2, ProjectRole.MEMBER));

			// then
			assertThat(projectMemberRepository.countByProjectIdAndRole(project1.getId(), ProjectRole.OWNER)).isEqualTo(1L);
			assertThat(projectMemberRepository.countByProjectIdAndRole(project1.getId(), ProjectRole.MEMBER)).isEqualTo(1L);
			assertThat(projectMemberRepository.countByProjectIdAndRole(project1.getId(), ProjectRole.ADMIN)).isEqualTo(0L);
		}

		@Test
		@DisplayName("성공: deleteAllByProjectId로 프로젝트의 모든 멤버를 일괄 삭제한다")
		void deleteAllByProjectId() {
			// given
			projectMemberRepository.save(ProjectMember.createOwner(project1, user1));
			projectMemberRepository.save(ProjectMember.createWithRole(project1, user2, ProjectRole.MEMBER));

			// when
			projectMemberRepository.deleteAllByProjectId(project1.getId());

			// then
			List<ProjectMember> remaining = projectMemberRepository.findAll();
			assertThat(remaining).isEmpty();
		}
	}
}
