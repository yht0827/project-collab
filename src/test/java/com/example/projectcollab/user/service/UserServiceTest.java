package com.example.projectcollab.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.user.dto.UserDto;
import com.example.projectcollab.user.entity.User;
import com.example.projectcollab.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Nested
	@DisplayName("사용자 등록 단위 테스트")
	class CreateUser {

		@Test
		@DisplayName("성공: 유효한 이름으로 사용자를 생성하여 저장한다")
		void success() {
			// given
			UserDto.CreateRequest request = new UserDto.CreateRequest("홍길동");
			User user = User.createUser("홍길동");
			ReflectionTestUtils.setField(user, "id", 1L);

			given(userRepository.save(any(User.class))).willReturn(user);

			// when
			UserDto.Response response = userService.createUser(request);

			// then
			assertThat(response.id()).isEqualTo(1L);
			assertThat(response.name()).isEqualTo("홍길동");
		}
	}

	@Nested
	@DisplayName("사용자 단건 조회 단위 테스트")
	class GetUser {

		@Test
		@DisplayName("성공: 존재하는 사용자를 조회한다")
		void success() {
			// given
			User user = User.createUser("김철수");
			ReflectionTestUtils.setField(user, "id", 2L);

			given(userRepository.findById(2L)).willReturn(Optional.of(user));

			// when
			UserDto.Response response = userService.getUser(2L);

			// then
			assertThat(response.id()).isEqualTo(2L);
			assertThat(response.name()).isEqualTo("김철수");
		}

		@Test
		@DisplayName("실패: 존재하지 않는 사용자 조회 시 USER_NOT_FOUND 예외가 발생한다")
		void failWhenUserNotFound() {
			// given
			given(userRepository.findById(999L)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.getUser(999L))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}
	}
}
