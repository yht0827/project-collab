package com.example.projectcollab.task.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.project.service.ProjectMemberService;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.entity.TaskStatus;
import com.example.projectcollab.task.repository.TaskRepository;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@InjectMocks
	private TaskService taskService;

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private ProjectMemberRepository projectMemberRepository;

	@Mock
	private ProjectMemberService projectMemberService;

	@Mock
	private UserService userService;

	@Nested
	@DisplayName("작업 생성 단위 테스트")
	class CreateTask {

		@Test
		@DisplayName("성공: 프로젝트 멤버는 담당자를 지정하여 작업을 생성할 수 있다")
		void success() {
			Long projectId = 100L;
			Long currentUserId = 1L;
			Long assigneeId = 2L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			User assignee = User.createUser("담당자");
			ReflectionTestUtils.setField(assignee, "id", assigneeId);

			Task task = Task.createTask(project, assignee, "제목", "설명");
			ReflectionTestUtils.setField(task, "id", 10L);

			TaskDto.CreateRequest request = new TaskDto.CreateRequest("제목", "설명", assigneeId);

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)).willReturn(true);
			given(userService.findUserById(assigneeId)).willReturn(assignee);
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)).willReturn(true);
			given(taskRepository.save(any(Task.class))).willReturn(task);

			TaskDto.Response response = taskService.createTask(currentUserId, projectId, request);

			assertThat(response.id()).isEqualTo(10L);
			assertThat(response.title()).isEqualTo("제목");
			assertThat(response.assigneeId()).isEqualTo(assigneeId);
		}

		@Test
		@DisplayName("실패: 프로젝트 멤버가 아닌 사용자를 담당자로 지정하면 ASSIGNEE_NOT_PROJECT_MEMBER 예외가 발생한다")
		void failWhenAssigneeNotMember() {
			Long projectId = 100L;
			Long currentUserId = 1L;
			Long nonMemberAssigneeId = 99L;

			Project project = Project.createProject("프로젝트", "설명");
			User nonMember = User.createUser("외부인");
			ReflectionTestUtils.setField(nonMember, "id", nonMemberAssigneeId);

			TaskDto.CreateRequest request = new TaskDto.CreateRequest("제목", "설명", nonMemberAssigneeId);

			given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)).willReturn(true);
			given(userService.findUserById(nonMemberAssigneeId)).willReturn(nonMember);
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, nonMemberAssigneeId)).willReturn(false);

			assertThatThrownBy(() -> taskService.createTask(currentUserId, projectId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSIGNEE_NOT_PROJECT_MEMBER);
		}
	}

	@Nested
	@DisplayName("작업 목록 조회 단위 테스트")
	class GetTasks {

		@Test
		@DisplayName("성공: 프로젝트 멤버는 작업 목록을 검색 조회할 수 있다")
		void success() {
			Long projectId = 100L;
			Long currentUserId = 1L;
			Pageable pageable = PageRequest.of(0, 10);
			TaskDto.SearchRequest condition = new TaskDto.SearchRequest(TaskStatus.TODO, "검색어");

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);
			Task task = Task.createTask(project, null, "제목", "설명");
			Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);

			given(projectRepository.existsById(projectId)).willReturn(true);
			given(projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUserId)).willReturn(true);
			given(taskRepository.searchTasks(projectId, TaskStatus.TODO, "검색어", pageable)).willReturn(taskPage);

			Page<TaskDto.Response> response = taskService.getTasks(currentUserId, projectId, condition, pageable);

			assertThat(response.getContent()).hasSize(1);
			assertThat(response.getContent().get(0).title()).isEqualTo("제목");
		}
	}

	@Nested
	@DisplayName("작업 수정 및 권한 단위 테스트")
	class UpdateTask {

		@Test
		@DisplayName("성공: 담당자 본인은 작업을 수정할 수 있다")
		void successForAssignee() {
			Long projectId = 100L;
			Long assigneeId = 2L;
			Long taskId = 10L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);

			User assignee = User.createUser("담당자");
			ReflectionTestUtils.setField(assignee, "id", assigneeId);

			ProjectMember assigneeMember = ProjectMember.createMember(project, assignee);
			Task task = Task.createTask(project, assignee, "제목", "설명");
			ReflectionTestUtils.setField(task, "id", taskId);

			TaskDto.UpdateRequest request = new TaskDto.UpdateRequest("수정 제목", "수정 내용", null, TaskStatus.IN_PROGRESS);

			given(projectMemberService.findMember(projectId, assigneeId)).willReturn(assigneeMember);
			given(taskRepository.findById(taskId)).willReturn(Optional.of(task));

			TaskDto.Response response = taskService.updateTask(assigneeId, projectId, taskId, request);

			assertThat(response.title()).isEqualTo("수정 제목");
			assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
		}

		@Test
		@DisplayName("실패: 담당자도 아니고 관리자(OWNER/ADMIN)도 아닌 일반 멤버는 수정 시 ACCESS_DENIED 예외가 발생한다")
		void failForOtherMember() {
			Long projectId = 100L;
			Long otherMemberId = 3L;
			Long assigneeId = 2L;
			Long taskId = 10L;

			Project project = Project.createProject("프로젝트", "설명");
			ReflectionTestUtils.setField(project, "id", projectId);

			User otherUser = User.createUser("다른멤버");
			User assignee = User.createUser("담당자");
			ReflectionTestUtils.setField(assignee, "id", assigneeId);

			ProjectMember otherMember = ProjectMember.createMember(project, otherUser);
			Task task = Task.createTask(project, assignee, "제목", "설명");
			ReflectionTestUtils.setField(task, "id", taskId);

			TaskDto.UpdateRequest request = new TaskDto.UpdateRequest("수정 제목", "수정 내용", null, TaskStatus.IN_PROGRESS);

			given(projectMemberService.findMember(projectId, otherMemberId)).willReturn(otherMember);
			given(taskRepository.findById(taskId)).willReturn(Optional.of(task));

			assertThatThrownBy(() -> taskService.updateTask(otherMemberId, projectId, taskId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
		}
	}
}
