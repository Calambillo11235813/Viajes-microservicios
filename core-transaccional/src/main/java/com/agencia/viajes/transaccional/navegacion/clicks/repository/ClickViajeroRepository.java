package com.agencia.viajes.transaccional.navegacion.clicks.repository;

import com.agencia.viajes.transaccional.navegacion.clicks.model.ClickViajeroItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * Repositorio write-only para persistir clicks en DynamoDB ({@code ClicksViajero}).
 */
@Repository
public class ClickViajeroRepository {

    private final DynamoDbTable<ClickViajeroItem> table;

    public ClickViajeroRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.clicks-table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(ClickViajeroItem.class));
    }

    /**
     * Persiste un click o visualización en DynamoDB.
     *
     * @param item registro a guardar.
     */
    public void guardar(ClickViajeroItem item) {
        table.putItem(item);
    }
}
