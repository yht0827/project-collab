package com.example.projectcollab.common.init;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.label.service.LabelService;
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
	private final LabelService labelService;

	@Override
	public void run(String... args) {
		log.info("============== [DataInitializer] 초기 샘플 데이터 적재 시작 ==============");

		// 1. 사용자 3명 등록
		UserDto.Response user1 = userService.createUser(new UserDto.CreateRequest("홍길동 (점장)"));
		UserDto.Response user2 = userService.createUser(new UserDto.CreateRequest("김철수 (바리스타)"));
		UserDto.Response user3 = userService.createUser(new UserDto.CreateRequest("이영희 (파티시에)"));

		// 2. 프로젝트 1: 성수동 플래그십 카페 1호점 오픈 준비 (홍길동: OWNER)
		ProjectDto.Response project1 = projectService.createProject(user1.id(), new ProjectDto.CreateRequest(
			"성수동 플래그십 카페 오픈 준비 ☕",
			"성수동 1호점 인테리어, 시그니처 메뉴 개발, 행정 신고 및 오픈 마케팅 준비 프로젝트"
		));

		// 3. 프로젝트 1 멤버 추가
		projectMemberService.addMember(user1.id(), project1.id(),
			new ProjectMemberDto.AddRequest(user2.id(), ProjectRole.ADMIN));
		projectMemberService.addMember(user1.id(), project1.id(),
			new ProjectMemberDto.AddRequest(user3.id(), ProjectRole.MEMBER));

		// 3-1. 프로젝트 1 라벨 생성 (인테리어, 메뉴 개발, 행정/허가, 긴급)
		LabelDto.Response labelInterior = labelService.createLabel(user1.id(), project1.id(),
			new LabelDto.CreateRequest("인테리어", "#3b82f6"));
		LabelDto.Response labelMenu = labelService.createLabel(user1.id(), project1.id(),
			new LabelDto.CreateRequest("메뉴 개발", "#10b981"));
		LabelDto.Response labelAdmin = labelService.createLabel(user1.id(), project1.id(),
			new LabelDto.CreateRequest("행정/허가", "#8b5cf6"));
		LabelDto.Response labelUrgent = labelService.createLabel(user1.id(), project1.id(),
			new LabelDto.CreateRequest("긴급", "#ef4444"));

		// 4. 프로젝트 1 작업(Task) 4건 등록 (마감일 및 라벨 포함)
		LocalDate today = LocalDate.now();

		// 작업 1: 보건증 발급 및 영업신고증 수령 (DONE)
		TaskDto.Response task1 = taskService.createTask(user1.id(), project1.id(), new TaskDto.CreateRequest(
			"보건증 발급 및 영업신고증 수령",
			"구청 위생과 방문하여 영업신고증 수령 및 사업자등록증 주소지 정정 완료",
			user1.id(),
			today.minusDays(2),
			List.of(labelAdmin.id())
		));
		taskService.updateTask(user1.id(), project1.id(), task1.id(), new TaskDto.UpdateRequest(
			task1.title(), task1.description(), user1.id(), TaskStatus.DONE, today.minusDays(2),
			List.of(labelAdmin.id())
		));

		// 작업 2: 에스프레소 머신 설치 및 원두 테이스팅 (IN_PROGRESS, D-3)
		TaskDto.Response task2 = taskService.createTask(user2.id(), project1.id(), new TaskDto.CreateRequest(
			"에스프레소 머신 설치 및 원두 테이스팅",
			"라마르조코 2그룹 머신 수평 및 압력 세팅, 다크 블렌드 에스프레소 추출 비율 점검",
			user2.id(),
			today.plusDays(3),
			List.of(labelInterior.id(), labelUrgent.id())
		));
		taskService.updateTask(user2.id(), project1.id(), task2.id(), new TaskDto.UpdateRequest(
			task2.title(), task2.description(), user2.id(), TaskStatus.IN_PROGRESS, today.plusDays(3),
			List.of(labelInterior.id(), labelUrgent.id())
		));

		// 작업 3: 시그니처 디저트 레시피 확정 및 메뉴판 인쇄 (IN_PROGRESS, D-Day)
		TaskDto.Response task3 = taskService.createTask(user3.id(), project1.id(), new TaskDto.CreateRequest(
			"시그니처 바스크 치즈케이크 레시피 확정",
			"글루텐프리 옥수수 바스크 치즈케이크 굽는 온도/시간 확정 및 쇼케이스 진열 테스트",
			user3.id(),
			today,
			List.of(labelMenu.id())
		));
		taskService.updateTask(user3.id(), project1.id(), task3.id(), new TaskDto.UpdateRequest(
			task3.title(), task3.description(), user3.id(), TaskStatus.IN_PROGRESS, today, List.of(labelMenu.id())
		));

		// 작업 4: 오픈 기념 1+1 이벤트 현수막 및 SNS 홍보 (TODO, D-5)
		taskService.createTask(user1.id(), project1.id(), new TaskDto.CreateRequest(
			"오픈 기념 아메리카노 1+1 이벤트 현수막 제작",
			"매장 전면 유리창 부착용 배너 발주 및 인스타그램 릴스 오픈 홍보 영상 업로드",
			user1.id(),
			today.plusDays(5),
			List.of(labelMenu.id(), labelUrgent.id())
		));

		// 5. 프로젝트 2: 2026 전사 가을 워크샵 기획 (김철수: OWNER, 홍길동: MEMBER)
		ProjectDto.Response project2 = projectService.createProject(user2.id(), new ProjectDto.CreateRequest(
			"2026 전사 가을 워크샵 기획 🍁",
			"가평 리조트 대관, 팀 빌딩 레크리에이션 및 저녁 바베큐 파티 기획 프로젝트"
		));
		projectMemberService.addMember(user2.id(), project2.id(),
			new ProjectMemberDto.AddRequest(user1.id(), ProjectRole.MEMBER));

		LabelDto.Response labelTrip = labelService.createLabel(user2.id(), project2.id(),
			new LabelDto.CreateRequest("장소/숙박", "#3b82f6"));
		LabelDto.Response labelFood = labelService.createLabel(user2.id(), project2.id(),
			new LabelDto.CreateRequest("식사/바베큐", "#f59e0b"));

		TaskDto.Response tripTask1 = taskService.createTask(user2.id(), project2.id(), new TaskDto.CreateRequest(
			"가평 리조트 독채 펜션 예약 완료",
			"세미나실 빔프로젝터 대여 및 수영장 온수풀 이용 사전 확약",
			user2.id(),
			today.minusDays(1),
			List.of(labelTrip.id())
		));
		taskService.updateTask(user2.id(), project2.id(), tripTask1.id(), new TaskDto.UpdateRequest(
			tripTask1.title(), tripTask1.description(), user2.id(), TaskStatus.DONE, today.minusDays(1),
			List.of(labelTrip.id())
		));

		taskService.createTask(user1.id(), project2.id(), new TaskDto.CreateRequest(
			"바베큐 파티 삼겹살/목살 및 주류 수량 취합",
			"총 15명 기준 고기 6kg, 쌈채소 및 음료 주문",
			user1.id(),
			today.plusDays(4),
			List.of(labelFood.id())
		));

		log.info("============== [DataInitializer] 초기 샘플 데이터 적재 완료 ==============");
	}
}
