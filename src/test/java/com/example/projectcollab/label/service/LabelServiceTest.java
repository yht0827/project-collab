package com.example.projectcollab.label.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.label.entity.Label;
import com.example.projectcollab.label.repository.LabelRepository;
import com.example.projectcollab.label.repository.TaskLabelRepository;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.project.service.ProjectMemberService;
import com.example.projectcollab.user.entity.User;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

	@InjectMocks
	private LabelService labelService;

	@Mock
	private LabelRepository labelRepository;

	@Mock
	private TaskLabelRepository taskLabelRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private ProjectMemberService projectMemberService;

	@Test
	@DisplayName("성공: 프로젝트 멤버는 프로젝트 라벨 목록을 조회할 수 있다")
	void getProjectLabelsSuccess() {
		Long currentUserId = 1L;
		Long projectId = 10L;

		Project project = Project.createProject("프로젝트", "설명");
		ReflectionTestUtils.setField(project, "id", projectId);

		Label label1 = Label.createLabel(project, "Backend", "#10b981");
		ReflectionTestUtils.setField(label1, "id", 100L);

		given(labelRepository.findByProjectIdOrderByNameAsc(projectId)).willReturn(List.of(label1));

		List<LabelDto.Response> responses = labelService.getProjectLabels(currentUserId, projectId);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).name()).isEqualTo("Backend");
		assertThat(responses.get(0).color()).isEqualTo("#10b981");
	}

	@Test
	@DisplayName("성공: 관리자(OWNER/ADMIN)는 신규 라벨을 생성할 수 있다")
	void createLabelSuccess() {
		Long currentUserId = 1L;
		Long projectId = 10L;

		Project project = Project.createProject("프로젝트", "설명");
		ReflectionTestUtils.setField(project, "id", projectId);

		User user = User.createUser("관리자");
		ReflectionTestUtils.setField(user, "id", currentUserId);

		ProjectMember managerMember = ProjectMember.createOwner(project, user);

		Label label = Label.createLabel(project, "Frontend", "#3b82f6");
		ReflectionTestUtils.setField(label, "id", 101L);

		LabelDto.CreateRequest request = new LabelDto.CreateRequest("Frontend", "#3b82f6");

		given(projectMemberService.validateManager(projectId, currentUserId)).willReturn(managerMember);
		given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
		given(labelRepository.existsByProjectIdAndName(projectId, "Frontend")).willReturn(false);
		given(labelRepository.save(any(Label.class))).willReturn(label);

		LabelDto.Response response = labelService.createLabel(currentUserId, projectId, request);

		assertThat(response.id()).isEqualTo(101L);
		assertThat(response.name()).isEqualTo("Frontend");
	}

	@Test
	@DisplayName("실패: 일반 멤버(MEMBER)가 라벨을 생성하려고 하면 ACCESS_DENIED 예외가 발생한다")
	void createLabelAccessDeniedForMember() {
		Long currentUserId = 2L;
		Long projectId = 10L;

		LabelDto.CreateRequest request = new LabelDto.CreateRequest("Design", "#8b5cf6");

		given(projectMemberService.validateManager(projectId, currentUserId))
			.willThrow(new BusinessException(ErrorCode.ACCESS_DENIED));

		assertThatThrownBy(() -> labelService.createLabel(currentUserId, projectId, request))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
	}
}
