package com.agencia.viajes.transaccional.gerencial.config;

import com.agencia.viajes.transaccional.usuarios.security.JwtTokenProvider;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de seguridad para proteger los endpoints REST bajo /api/gerencial/**
 * Solo permite el acceso si el token JWT es válido y el usuario tiene el rol de GERENTE (id_rol = 2).
 */
@Component
@RequiredArgsConstructor
public class GerencialSecurityFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private static final String ROL_GERENTE = "GERENTE"; // Asumiendo que "GERENTE" es el nombre del rol

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/gerencial")) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String jwt = bearerToken.substring(7);
                if (tokenProvider.validateToken(jwt)) {
                    UsuarioAutenticado usuario = tokenProvider.getUsuarioFromJWT(jwt);
                    if (ROL_GERENTE.equals(usuario.getRol())) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado. Se requiere rol de GERENTE.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
