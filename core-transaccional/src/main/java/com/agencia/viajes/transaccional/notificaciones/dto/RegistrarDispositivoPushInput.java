package com.agencia.viajes.transaccional.notificaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Input GraphQL para registrar token push de un dispositivo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarDispositivoPushInput {
    private Integer idUsuario;
    private String token;
    private String plataforma;
}
