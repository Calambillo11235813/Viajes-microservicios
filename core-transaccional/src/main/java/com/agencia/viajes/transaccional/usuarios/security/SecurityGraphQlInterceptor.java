package com.agencia.viajes.transaccional.usuarios.security;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityGraphQlInterceptor implements WebGraphQlInterceptor {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (bearerToken.startsWith("Bearer ")) {
                String jwt = bearerToken.substring(7);
                if (tokenProvider.validateToken(jwt)) {
                    UsuarioAutenticado usuario = tokenProvider.getUsuarioFromJWT(jwt);
                    request.configureExecutionInput((executionInput, builder) -> {
                        builder.graphQLContext(contextBuilder -> contextBuilder.put("usuarioAutenticado", usuario));
                        return builder.build();
                    });
                }
            }
        }
        return chain.next(request);
    }
}
