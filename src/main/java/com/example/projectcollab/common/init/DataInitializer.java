package com.example.projectcollab.common.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.projectcollab.project.dto.ProjectDto;
import com.example.projectcollab.project.dto.ProjectMemberDto;
import com.example.projectcollab.project.entity.ProjectRole;
import com.example.projectcollab.project.service.ProjectMemberService;
import com.example.projectcollab.project.service.ProjectService;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.TaskStatus;
import com.example.projectcollab.task.service.TaskService;
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final UserService userService;
	private final ProjectService projectService;
	private final ProjectMemberService projectMemberService;
	private final TaskService taskService;

	@Override
	public void run(String... args) {
		log.info("============== [DataInitializer] 초기 샘플 데이터 적재 시작 ==============");

		// 1. 사용자 3명 등록
		UserDto.Response user1 = userService.createUser(new UserDto.CreateRequest("홍길동"));
		UserDto.Response user2 = userService.createUser(new UserDto.CreateRequest("김철수"));
		UserDto.Response user3 = userService.createUser(new UserDto.CreateRequest("이영희"));

		// 2. 프로젝트 1 생성 (홍길동: OWNER)
		ProjectDto.Response project1 = projectService.createProject(user1.id(), new ProjectDto.CreateRequest(
			"협업 플랫폼 개발",
			"실시간 협업 및 칸반 보드를 지원하는 백엔드 서비스 개발 프로젝트"
		));

		// 3. 프로젝트 1 멤버 추가
		projectMemberService.addMember(user1.id(), project1.id(), new ProjectMemberDto.AddRequest(user2.id(), ProjectRole.ADMIN));
		projectMemberService.addMember(user1.id(), project1.id(), new ProjectMemberDto.AddRequest(user3.id(), ProjectRole.MEMBER));

		// 4. 프로젝트 1 작업(Task) 3건 등록
		TaskDto.Response task1 = taskService.createTask(user1.id(), project1.id(), new TaskDto.CreateRequest(
			"요구사항 분석 및 API 설계",
			"기능 명세서 및 Swagger API 문서 작성",
			user1.id()
		));
		taskService.updateTask(user1.id(), project1.id(), task1.id(), new TaskDto.UpdateRequest(
			task1.title(), task1.description(), user1.id(), TaskStatus.DONE
		));

		TaskDto.Response task2 = taskService.createTask(user2.id(), project1.id(), new TaskDto.CreateRequest(
			"Spring Boot 백엔드 구현",
			"도메인 엔티티 설계, 동시성 제어 및 단위/통합 테스트 작성",
			user2.id()
		));
		taskService.updateTask(user2.id(), project1.id(), task2.id(), new TaskDto.UpdateRequest(
			task2.title(), task2.description(), user2.id(), TaskStatus.IN_PROGRESS
		));

		taskService.createTask(user3.id(), project1.id(), new TaskDto.CreateRequest(
			"Swagger API 문서화 및 검증",
			"OpenAPI 3.0 사양 검증 및 Swagger UI 동작 테스트",
			user3.id()
		));

		// 5. 프로젝트 2 생성 (김철수: OWNER, 홍길동: MEMBER)
		ProjectDto.Response project2 = projectService.createProject(user2.id(), new ProjectDto.CreateRequest(
			"차세대 인프라 구축",
			"AWS EKS 및 CI/CD 파이프라인 고도화 프로젝트"
		));
		projectMemberService.addMember(user2.id(), project2.id(), new ProjectMemberDto.AddRequest(user1.id(), ProjectRole.MEMBER));

		log.info("============== [DataInitializer] 초기 샘플 데이터 적재 완료 ==============");
	}
}
