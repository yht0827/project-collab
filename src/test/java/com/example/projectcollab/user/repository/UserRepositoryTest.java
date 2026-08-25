package com.example.projectcollab.user.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.projectcollab.common.config.JpaConfig;
import com.example.projectcollab.user.entity.User;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Nested
	@DisplayName("사용자 저장 및 Auditing 검증")
	class SaveAndAuditing {

		@Test
		@DisplayName("성공: 사용자를 저장하면 ID가 생성되고 createdAt, updatedAt이 자동으로 기록된다")
		void saveUserWithAuditing() {
			// given
			User user = User.createUser("홍길동");

			// when
			User savedUser = userRepository.save(user);

			// then
			assertThat(savedUser.getId()).isNotNull();
			assertThat(savedUser.getName()).isEqualTo("홍길동");
			assertThat(savedUser.getCreatedAt()).isNotNull();
			assertThat(savedUser.getUpdatedAt()).isNotNull();
		}
	}

	@Nested
	@DisplayName("사용자 조회 검증")
	class FindById {

		@Test
		@DisplayName("성공: 저장된 사용자를 ID로 조회한다")
		void findByIdSuccess() {
			// given
			User user = userRepository.save(User.createUser("김철수"));

			// when
			Optional<User> foundUser = userRepository.findById(user.getId());

			// then
			assertThat(foundUser).isPresent();
			assertThat(foundUser.get().getId()).isEqualTo(user.getId());
			assertThat(foundUser.get().getName()).isEqualTo("김철수");
		}

		@Test
		@DisplayName("실패: 존재하지 않는 ID로 조회하면 Optional.empty()를 반환한다")
		void findByIdNotFound() {
			// when
			Optional<User> foundUser = userRepository.findById(999999L);

			// then
			assertThat(foundUser).isEmpty();
		}
	}
}
