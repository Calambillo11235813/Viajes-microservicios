package com.agencia.viajes.transaccional.navegacion.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Entidad DynamoDB para eventos de navegación del viajero.
 * Mapea la tabla {@code NavegacionViajes} con PK {@code id_usuario} y SK {@code timestamp}.
 */
@DynamoDbBean
@Data
@NoArgsConstructor
public class NavegacionItem {

    private Integer idUsuario;
    private String timestamp;
    private String idInteraccion;
    private String tipoEvento;
    private String tipoAccion;
    private String origen;
    private String destino;
    private String fechaBusqueda;
    private Integer idRuta;
    private Integer resultadosCount;
    private String canal;
    private String dispositivo;
    private Integer tiempoPermanenciaSeg;
    private String categoriaVista;
    private Integer idRutaVista;
    private String ciudadOrigenVista;
    private String ciudadDestinoVista;
    private String metadata;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id_usuario")
    public Integer getIdUsuario() {
        return idUsuario;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @DynamoDbAttribute("id_interaccion")
    public String getIdInteraccion() {
        return idInteraccion;
    }

    @DynamoDbAttribute("tipo_evento")
    public String getTipoEvento() {
        return tipoEvento;
    }

    @DynamoDbAttribute("tipo_accion")
    public String getTipoAccion() {
        return tipoAccion;
    }

    @DynamoDbAttribute("fecha_busqueda")
    public String getFechaBusqueda() {
        return fechaBusqueda;
    }

    @DynamoDbAttribute("id_ruta")
    public Integer getIdRuta() {
        return idRuta;
    }

    @DynamoDbAttribute("resultados_count")
    public Integer getResultadosCount() {
        return resultadosCount;
    }

    @DynamoDbAttribute("tiempo_permanencia_seg")
    public Integer getTiempoPermanenciaSeg() {
        return tiempoPermanenciaSeg;
    }

    @DynamoDbAttribute("categoria_vista")
    public String getCategoriaVista() {
        return categoriaVista;
    }

    @DynamoDbAttribute("id_ruta_vista")
    public Integer getIdRutaVista() {
        return idRutaVista;
    }

    @DynamoDbAttribute("ciudad_origen_vista")
    public String getCiudadOrigenVista() {
        return ciudadOrigenVista;
    }

    @DynamoDbAttribute("ciudad_destino_vista")
    public String getCiudadDestinoVista() {
        return ciudadDestinoVista;
    }
}
