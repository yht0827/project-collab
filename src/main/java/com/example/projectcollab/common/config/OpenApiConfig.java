package com.example.projectcollab.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	public static final String USER_ID_HEADER = "X-User-Id";

	@Bean
	public OpenAPI projectCollabOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Project Collab API")
				.description("프로젝트와 작업을 여러 사용자가 함께 관리하는 협업 서비스 API 명세서")
				.version("v1.0.0"))
			.addSecurityItem(new SecurityRequirement().addList(USER_ID_HEADER))
			.components(new Components()
				.addSecuritySchemes(USER_ID_HEADER, new SecurityScheme()
					.name(USER_ID_HEADER)
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.HEADER)
					.description("요청자 식별자 (User ID)")));
	}
}
