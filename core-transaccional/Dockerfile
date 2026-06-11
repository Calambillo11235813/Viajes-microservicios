FROM eclipse-temurin:17-jdk-alpine

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR generado al contenedor. 
# NOTA: Primero debes compilar el proyecto con `.\gradlew.bat bootJar`
COPY build/libs/*-SNAPSHOT.jar app.jar

# Exponer el puerto (ajustable según tu entorno, 9090 o 8080)
EXPOSE 9090

# Comando para ejecutar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
