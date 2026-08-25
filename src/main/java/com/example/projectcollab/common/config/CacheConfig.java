package com.example.projectcollab.common.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	/**
	 * 실무 표준 Caffeine Cache 매니저 설정
	 * - expireAfterWrite: 쓰기 후 10분 뒤 자동 만료 (TTL)
	 * - maximumSize: 최대 1,000개 캐싱 (OOM 방지 및 TinyLFU 기반 퇴출)
	 * - recordStats: 캐시 히트율 모니터링 활성화
	 */
	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("users");
		cacheManager.setCaffeine(Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.maximumSize(1_000)
			.recordStats());
		return cacheManager;
	}
}
