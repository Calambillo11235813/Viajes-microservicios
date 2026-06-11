package com.agencia.viajes.transaccional.navegacion.feedback.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Entidad DynamoDB para comentarios y calificaciones post-viaje.
 * Mapea la tabla {@code FeedbackViajero} con PK {@code id_usuario} y SK {@code id_viaje}.
 */
@DynamoDbBean
@Data
@NoArgsConstructor
public class FeedbackViajeroItem {

    private Integer idUsuario;
    private Integer idViaje;
    private String fecha;
    private Integer calificacion;
    private String comentario;
    private Integer idReserva;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id_usuario")
    public Integer getIdUsuario() {
        return idUsuario;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("id_viaje")
    public Integer getIdViaje() {
        return idViaje;
    }

    @DynamoDbAttribute("id_reserva")
    public Integer getIdReserva() {
        return idReserva;
    }
}
