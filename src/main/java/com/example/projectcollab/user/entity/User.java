package com.example.projectcollab.user.entity;

import com.example.projectcollab.common.entity.BaseTimeEntity;
import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	public static final int MAX_NAME_LENGTH = 50;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = MAX_NAME_LENGTH)
	private String name;

	private User(String name) {
		this.name = name;
	}

	// 사용자 생성
	public static User createUser(String name) {
		validateName(name);
		return new User(name);
	}

	private static void validateName(String name) {
		if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
