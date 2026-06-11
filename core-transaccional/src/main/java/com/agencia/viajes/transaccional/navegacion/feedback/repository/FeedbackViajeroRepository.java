package com.agencia.viajes.transaccional.navegacion.feedback.repository;

import com.agencia.viajes.transaccional.navegacion.feedback.model.FeedbackViajeroItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * Repositorio write-only para persistir feedback en DynamoDB ({@code FeedbackViajero}).
 */
@Repository
public class FeedbackViajeroRepository {

    private final DynamoDbTable<FeedbackViajeroItem> table;

    public FeedbackViajeroRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.feedback-table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(FeedbackViajeroItem.class));
    }

    /**
     * Persiste un comentario/calificación en DynamoDB.
     *
     * @param item registro a guardar.
     */
    public void guardar(FeedbackViajeroItem item) {
        table.putItem(item);
    }
}
