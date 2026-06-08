package com.agencia.viajes.transaccional.usuarios.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAutenticado {
    private Integer idUsuario;
    private String email;
    private String rol;
}
