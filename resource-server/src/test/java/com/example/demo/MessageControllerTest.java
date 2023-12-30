package com.example.demo;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * @author Josh Cummings
 */
@WebFluxTest(MessageController.class)
@Import(SecurityConfig.class)
public class MessageControllerTest {

    @Autowired
    WebTestClient rest;

    @MockBean
    ReactiveJwtDecoder jwtDecoder;

    @Test
    void indexGreetsAuthenticatedUser() {
        // @formatter:off
        this.rest.mutateWith(mockJwt().jwt((jwt) -> jwt.subject("test-subject")))
                .get()
                .uri("/")
                .exchange()
                .expectBody(String.class).isEqualTo("Hello, test-subject!");
        // @formatter:on
    }

    @Test
    void messageCanBeReadWithScopeMessageReadAuthority() {
        // @formatter:off
        this.rest.mutateWith(mockJwt().jwt((jwt) -> jwt.claim("scope", "message:read")))
                .get()
                .uri("/message")
                .exchange()
                .expectBody(String.class).isEqualTo("secret message");

        this.rest.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_message:read")))
                .get()
                .uri("/message")
                .exchange()
                .expectBody(String.class).isEqualTo("secret message");
        // @formatter:on
    }

    @Test
    void messageCanNotBeReadWithoutScopeMessageReadAuthority() {
        // @formatter:off
        this.rest.mutateWith(mockJwt())
                .get()
                .uri("/message")
                .exchange()
                .expectStatus().isForbidden();
        // @formatter:on
    }

    @Test
    void messageCanNotBeCreatedWithoutAnyScope() {
        Jwt jwt = jwt().claim("scope", "").build();
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
        // @formatter:off
        this.rest.post()
                .uri("/message")
                .headers((headers) -> headers.setBearerAuth(jwt.getTokenValue()))
                .bodyValue("Hello message")
                .exchange()
                .expectStatus().isForbidden();
        // @formatter:on
    }

    @Test
    void messageCanNotBeCreatedWithScopeMessageReadAuthority() {
        Jwt jwt = jwt().claim("scope", "message:read").build();
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
        // @formatter:off
        this.rest.post()
                .uri("/message")
                .headers((headers) -> headers.setBearerAuth(jwt.getTokenValue()))
                .bodyValue("Hello message")
                .exchange()
                .expectStatus().isForbidden();
        // @formatter:on
    }

    @Test
    void messageCanBeCreatedWithScopeMessageWriteAuthority() {
        Jwt jwt = jwt().claim("scope", "message:write").build();
        when(this.jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));
        // @formatter:off
        this.rest.post()
                .uri("/message")
                .headers((headers) -> headers.setBearerAuth(jwt.getTokenValue()))
                .bodyValue("Hello message")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Message was created. Content: Hello message");
        // @formatter:on
    }

    private Jwt.Builder jwt() {
        return Jwt.withTokenValue("token").header("alg", "none");
    }

}
