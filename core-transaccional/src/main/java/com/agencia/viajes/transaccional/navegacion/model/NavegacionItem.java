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
    private String tipoEvento;
    private String origen;
    private String destino;
    private String fechaBusqueda;
    private Integer idRuta;
    private Integer resultadosCount;
    private String canal;
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
}
