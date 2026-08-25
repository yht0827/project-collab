package com.example.projectcollab.common.resolver;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;

class CurrentUserIdArgumentResolverTest {

	private final CurrentUserIdArgumentResolver resolver = new CurrentUserIdArgumentResolver();
	private MethodParameter requiredParam;
	private MethodParameter optionalParam;
	private MethodParameter nonAnnotatedParam;

	@BeforeEach
	void setUp() throws NoSuchMethodException {
		Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
		requiredParam = new MethodParameter(method, 0);
		optionalParam = new MethodParameter(method, 1);
		nonAnnotatedParam = new MethodParameter(method, 2);
	}

	@Test
	@DisplayName("지원 여부: @CurrentUserId 파라미터만 지원한다")
	void supportsParameter() {
		assertThat(resolver.supportsParameter(requiredParam)).isTrue();
		assertThat(resolver.supportsParameter(nonAnnotatedParam)).isFalse();
	}

	@Test
	@DisplayName("X-User-Id 헤더에서 사용자 ID를 정상 추출한다")
	void resolveFromHeader() {
		Object result = resolve(requiredParam, "X-User-Id", "123", true);
		assertThat(result).isEqualTo(123L);
	}

	@Test
	@DisplayName("쿼리 파라미터(currentUserId)에서 사용자 ID를 정상 추출한다")
	void resolveFromQueryParam() {
		Object result = resolve(requiredParam, "currentUserId", "456", false);
		assertThat(result).isEqualTo(456L);
	}

	@Test
	@DisplayName("숫자가 아닌 잘못된 ID가 전달되면 INVALID_INPUT_VALUE 예외가 발생한다")
	void failWhenInvalidNumber() {
		assertThatThrownBy(() -> resolve(requiredParam, "X-User-Id", "abc", true))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
	}

	@Test
	@DisplayName("required=true일 때 식별자가 없으면 UNAUTHORIZED 예외가 발생한다")
	void failWhenMissingRequired() {
		assertThatThrownBy(() -> resolve(requiredParam, null, null, false))
			.isInstanceOf(BusinessException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
	}

	@Test
	@DisplayName("required=false일 때 식별자가 없으면 null을 반환한다")
	void returnNullWhenOptional() {
		Object result = resolve(optionalParam, null, null, false);
		assertThat(result).isNull();
	}

	private Object resolve(MethodParameter param, String key, String value, boolean isHeader) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		if (key != null && value != null) {
			if (isHeader) {
				request.addHeader(key, value);
			} else {
				request.setParameter(key, value);
			}
		}
		return resolver.resolveArgument(param, null, new ServletWebRequest(request), null);
	}

	@SuppressWarnings("unused")
	private void sampleMethod(
		@CurrentUserId Long required,
		@CurrentUserId(required = false) Long optional,
		String nonAnnotated
	) {
	}
}
