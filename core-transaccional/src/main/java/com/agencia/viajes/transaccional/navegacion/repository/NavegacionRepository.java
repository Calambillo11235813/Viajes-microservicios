package com.agencia.viajes.transaccional.navegacion.repository;

import com.agencia.viajes.transaccional.navegacion.model.NavegacionItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * Repositorio write-only para persistir eventos de navegación en DynamoDB.
 */
@Repository
public class NavegacionRepository {

    private final DynamoDbTable<NavegacionItem> table;

    public NavegacionRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(NavegacionItem.class));
    }

    /**
     * Persiste un evento de navegación en DynamoDB.
     *
     * @param item registro a guardar.
     */
    public void guardar(NavegacionItem item) {
        table.putItem(item);
    }
}
