package com.agencia.viajes.transaccional.navegacion.clicks.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Entidad DynamoDB para clicks y visualizaciones del viajero.
 * Mapea la tabla {@code ClicksViajero} con PK {@code id_usuario} y SK {@code timestamp}.
 */
@DynamoDbBean
@Data
@NoArgsConstructor
public class ClickViajeroItem {

    private Integer idUsuario;
    private String timestamp;
    private String fecha;
    private String tipoEvento;
    private Integer idRuta;
    private Integer tiempoPermanenciaSeg;
    private String canal;
    private String dispositivo;

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

    @DynamoDbAttribute("tipo_evento")
    public String getTipoEvento() {
        return tipoEvento;
    }

    @DynamoDbAttribute("id_ruta")
    public Integer getIdRuta() {
        return idRuta;
    }

    @DynamoDbAttribute("tiempo_permanencia_seg")
    public Integer getTiempoPermanenciaSeg() {
        return tiempoPermanenciaSeg;
    }
}
