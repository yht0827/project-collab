package com.example.projectcollab.common.logging;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String uri = request.getRequestURI();

		// Swagger, H2 Console, 정적 리소스는 상세 로깅에서 제외
		if (isExcluded(uri)) {
			filterChain.doFilter(request, response);
			return;
		}

		long startTime = System.currentTimeMillis();
		String method = request.getMethod();
		String queryString = request.getQueryString();
		String fullUrl = (queryString != null) ? uri + "?" + queryString : uri;
		String userId = request.getHeader("X-User-Id");

		log.info("[HTTP REQ] {} {} | userId={}", method, fullUrl, (userId != null ? userId : "anonymous"));

		try {
			filterChain.doFilter(request, response);
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			int status = response.getStatus();

			log.info("[HTTP RES] {} {} | status={} ({}ms) | userId={}", method, fullUrl, status, duration,
				(userId != null ? userId : "anonymous"));
		}
	}

	private boolean isExcluded(String uri) {
		return uri.startsWith("/swagger-ui") ||
			uri.startsWith("/v3/api-docs") ||
			uri.startsWith("/h2-console") ||
			uri.startsWith("/favicon.ico");
	}
}
