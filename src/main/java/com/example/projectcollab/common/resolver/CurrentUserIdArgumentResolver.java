package com.example.projectcollab.common.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String HEADER_X_USER_ID = "X-User-Id";
	public static final String PARAM_CURRENT_USER_ID = "currentUserId";
	public static final String PARAM_USER_ID = "userId";
	public static final String PARAM_REQUESTER_ID = "requesterId";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentUserId.class)
			&& (parameter.getParameterType().equals(Long.class) || parameter.getParameterType().equals(long.class));
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory) {

		CurrentUserId annotation = parameter.getParameterAnnotation(CurrentUserId.class);
		boolean required = (annotation != null) && annotation.required();

		// 1. Header (X-User-Id) 확인
		String headerValue = webRequest.getHeader(HEADER_X_USER_ID);
		if (StringUtils.hasText(headerValue)) {
			return parseLong(headerValue);
		}

		// 2. Query/Form Parameter (currentUserId 또는 userId 또는 requesterId) 확인
		String paramValue = webRequest.getParameter(PARAM_CURRENT_USER_ID);
		if (!StringUtils.hasText(paramValue)) {
			paramValue = webRequest.getParameter(PARAM_USER_ID);
		}
		if (!StringUtils.hasText(paramValue)) {
			paramValue = webRequest.getParameter(PARAM_REQUESTER_ID);
		}

		if (StringUtils.hasText(paramValue)) {
			return parseLong(paramValue);
		}

		if (required) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		return null;
	}

	private Long parseLong(String value) {
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
