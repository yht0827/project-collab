package com.example.projectcollab.task.concurrency;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.projectcollab.label.repository.LabelRepository;
import com.example.projectcollab.label.repository.TaskLabelRepository;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectMemberRepository;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.task.dto.TaskDto;
import com.example.projectcollab.task.entity.Task;
import com.example.projectcollab.task.entity.TaskStatus;
import com.example.projectcollab.task.repository.TaskRepository;
import com.example.projectcollab.task.service.TaskService;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.repository.UserRepository;

@SpringBootTest
class TaskConcurrencyTest {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private TaskLabelRepository taskLabelRepository;

	private User userA;
	private User userB;
	private Project project;
	private Task task;

	@BeforeEach
	void setUp() {
		userA = userRepository.save(User.createUser("사용자A"));
		userB = userRepository.save(User.createUser("사용자B"));

		project = projectRepository.save(Project.createProject("동시성 프로젝트", "설명"));
		projectMemberRepository.save(ProjectMember.createOwner(project, userA));
		projectMemberRepository.save(ProjectMember.createOwner(project, userB));

		task = taskRepository.save(Task.createTask(project, userA, "초기 제목", "초기 내용"));
	}

	@AfterEach
	void tearDown() {
		taskLabelRepository.deleteAll();
		labelRepository.deleteAll();
		taskRepository.deleteAll();
		projectMemberRepository.deleteAll();
		projectRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("동시성 검증: 두 사용자가 동일한 작업을 동시에 수정하면 한 요청만 성공하고 다른 요청은 낙관적 락 충돌(OptimisticLocking) 예외가 발생한다")
	void concurrentTaskUpdateTest() throws InterruptedException {
		int numberOfThreads = 2;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger conflictCount = new AtomicInteger(0);

		// 스레드 1: 사용자 A의 수정 요청
		executorService.submit(() -> {
			try {
				readyLatch.countDown();
				startLatch.await(); // 두 스레드가 동시에 출발하도록 대기

				TaskDto.UpdateRequest requestA = new TaskDto.UpdateRequest("사용자 A가 수정한 제목", "설명 A", userA.getId(),
					TaskStatus.IN_PROGRESS);
				taskService.updateTask(userA.getId(), project.getId(), task.getId(), requestA);
				successCount.incrementAndGet();
			} catch (ObjectOptimisticLockingFailureException e) {
				conflictCount.incrementAndGet();
			} catch (Exception e) {
				if (e.getCause() instanceof ObjectOptimisticLockingFailureException || e.getMessage()
					.contains("OptimisticLock")) {
					conflictCount.incrementAndGet();
				}
			} finally {
				doneLatch.countDown();
			}
		});

		// 스레드 2: 사용자 B의 수정 요청
		executorService.submit(() -> {
			try {
				readyLatch.countDown();
				startLatch.await(); // 두 스레드가 동시에 출발하도록 대기

				TaskDto.UpdateRequest requestB = new TaskDto.UpdateRequest("사용자 B가 수정한 제목", "설명 B", userB.getId(),
					TaskStatus.DONE);
				taskService.updateTask(userB.getId(), project.getId(), task.getId(), requestB);
				successCount.incrementAndGet();
			} catch (ObjectOptimisticLockingFailureException e) {
				conflictCount.incrementAndGet();
			} catch (Exception e) {
				if (e.getCause() instanceof ObjectOptimisticLockingFailureException || e.getMessage()
					.contains("OptimisticLock")) {
					conflictCount.incrementAndGet();
				}
			} finally {
				doneLatch.countDown();
			}
		});

		readyLatch.await();
		startLatch.countDown(); // 동시 실행 시작
		doneLatch.await();

		// then: 둘 중 하나는 성공하고, 하나는 낙관적 락 충돌로 실패해야 함
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(conflictCount.get()).isEqualTo(1);

		// DB의 최종 작업 버전이 1인지 확인
		Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
		assertThat(updatedTask.getVersion()).isEqualTo(1L);
	}
}
