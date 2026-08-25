package com.example.projectcollab.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;

class UserTest {

	@Nested
	@DisplayName("User 생성 단위 테스트")
	class CreateUserTest {

		@Test
		@DisplayName("성공: 최대 글자 수 이하의 정상 이름으로 생성된다")
		void success() {
			User user = User.createUser("홍길동");
			assertThat(user.getName()).isEqualTo("홍길동");
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {"   ", "\t", "\n"})
		@DisplayName("실패: 이름이 null이거나 공백이면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenNameIsBlank(String invalidName) {
			assertThatThrownBy(() -> User.createUser(invalidName))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}

		@Test
		@DisplayName("실패: 이름이 최대 글자 수를 초과하면 INVALID_INPUT_VALUE 예외가 발생한다")
		void failWhenNameExceedsMaxLength() {
			String tooLongName = "a".repeat(User.MAX_NAME_LENGTH + 1);
			assertThatThrownBy(() -> User.createUser(tooLongName))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
