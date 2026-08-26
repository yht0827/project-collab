package com.example.projectcollab.user.service;

import java.util.List;

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
		User user = User.createUser(request.name());
		User savedUser = userRepository.save(user);
		return UserDto.Response.from(savedUser);
	}

	// 사용자 단건 상세 조회
	public UserDto.Response getUser(Long userId) {
		User user = findUserById(userId);
		return UserDto.Response.from(user);
	}

	// 전체 사용자 목록 조회
	public List<UserDto.Response> getAllUsers() {
		return userRepository.findAll().stream()
			.map(UserDto.Response::from)
			.toList();
	}

	public User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
