### Redis (para eventos asíncronos)

En producción (AWS), se utiliza Amazon ElastiCache for Redis.

**Configuración:**
1. Crear un clúster Redis (versión 7.x) en ElastiCache.
2. Copiar el endpoint primario.
3. En `application.properties` del core-transaccional, reemplazar:
   ```properties
   spring.redis.host=redis.xxxxxx.amazonaws.com
   spring.redis.port=6379