package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication
public class OAuth2ResourceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAuth2ResourceServerApplication.class, args);
	}

}


@Configuration
class SecurityConfig {

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		// @formatter:off
		http
			.authorizeExchange((authorize) -> authorize
					.pathMatchers(HttpMethod.GET, "/message/**").hasAuthority("SCOPE_message:read")
					.pathMatchers(HttpMethod.POST, "/message/**").hasAuthority("SCOPE_message:write")
					.anyExchange().authenticated()
			)
			.oauth2ResourceServer((resourceServer) -> resourceServer
					.jwt(withDefaults())
			);
		// @formatter:on
		return http.build();
	}
}