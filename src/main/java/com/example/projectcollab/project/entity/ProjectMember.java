package com.example.projectcollab.project.entity;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "project_members",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_project_member",
			columnNames = {"project_id", "user_id"}
		)
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProjectRole role;

	private ProjectMember(Project project, User user, ProjectRole role) {
		this.project = project;
		this.user = user;
		this.role = role;
	}

	// 프로젝트 소유자(Owner) 멤버 생성
	public static ProjectMember createOwner(Project project, User user) {
		validateInputs(project, user);
		return new ProjectMember(project, user, ProjectRole.OWNER);
	}

	// 일반 참여자(Member) 멤버 생성
	public static ProjectMember createMember(Project project, User user) {
		validateInputs(project, user);
		return new ProjectMember(project, user, ProjectRole.MEMBER);
	}

	// 지정된 역할로 멤버 생성
	public static ProjectMember createWithRole(Project project, User user, ProjectRole role) {
		validateInputs(project, user);
		if (role == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return new ProjectMember(project, user, role);
	}

	private static void validateInputs(Project project, User user) {
		if (project == null || user == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	// 프로젝트 멤버 역할(권한) 변경
	public void updateRole(ProjectRole role) {
		if (role == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		this.role = role;
	}

	// 소유자 여부 확인
	public boolean isOwner() {
		return this.role == ProjectRole.OWNER;
	}

	// 관리자 이상 권한 (OWNER 또는 ADMIN) 여부 확인
	public boolean isManager() {
		return this.role == ProjectRole.OWNER || this.role == ProjectRole.ADMIN;
	}
}
