package com.example.projectcollab.common.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 API를 요청한 사용자(로그인 사용자)의 식별자(User ID)를 컨트롤러 파라미터로 주입받기 위한 어노테이션.
 * Header(X-User-Id) 또는 Query Parameter(userId, currentUserId)에서 값을 추출합니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {

	boolean required() default true;
}
