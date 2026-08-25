package com.example.projectcollab.user.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	// 신규 사용자 등록
	@Transactional
	public UserDto.Response createUser(UserDto.CreateRequest request) {
		// 1. 사용자 엔티티 생성 및 저장
		User user = User.createUser(request.name());
		User savedUser = userRepository.save(user);

		// 2. DTO 변환 후 반환
		return UserDto.Response.from(savedUser);
	}

	// 사용자 단건 상세 조회 (변경 빈도가 낮고 조회가 빈번하여 1차 로컬 캐시 적용)
	@Cacheable(value = "users", key = "#userId")
	public UserDto.Response getUser(Long userId) {
		// 1. 사용자 엔티티 조회
		User user = findUserById(userId);

		// 2. DTO 변환 후 반환
		return UserDto.Response.from(user);
	}

	public User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
