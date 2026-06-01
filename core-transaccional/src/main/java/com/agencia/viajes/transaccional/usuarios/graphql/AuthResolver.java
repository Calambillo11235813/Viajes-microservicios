package com.agencia.viajes.transaccional.usuarios.graphql;

import com.agencia.viajes.transaccional.usuarios.dto.AuthResponse;
import com.agencia.viajes.transaccional.usuarios.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Operaciones GraphQL para Autenticación (Login).
 */
@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;

    /**
     * Inicia sesión en el sistema
     *
     * @param email Correo electrónico
     * @param passwordHash Contraseña ingresada
     * @return Respuesta de autenticación con token
     */
    @MutationMapping
    public AuthResponse login(@Argument String email, @Argument String passwordHash) {
        return authService.autenticar(email, passwordHash);
    }
}