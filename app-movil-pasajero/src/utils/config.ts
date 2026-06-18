/**
 * Configuración Global de la Aplicación
 *
 * Centraliza la dirección IP y puertos para los diferentes microservicios.
 * Si pruebas desde un dispositivo físico (Android/iOS), cambia LOCAL_IP
 * por la dirección IP de tu computadora en la red local.
 * Debe coincidir con REACT_NATIVE_PACKAGER_HOSTNAME en el archivo .env
 * (para que Metro/Expo muestre tu IP en lugar de localhost:8081).
 */

// IP de tu computadora para el Core Transaccional (Spring Boot)
const CORE_IP = '192.168.0.5';

// IP de la máquina virtual en Google Cloud para el Motor de IA (Django)
// REEMPLAZA esta IP con la IP Externa que te dio Google Cloud:
const IA_IP = '34.75.80.130';
// Recuerda que en el docker-compose de GCP expusimos la IA en el puerto 8080
const IA_PORT = '8080';

export const CONFIG = {
  // Microservicio A: Spring Boot (GraphQL - Transaccional)
  GRAPHQL_URL: `http://${CORE_IP}:9090/graphql`,

  // Microservicio B: Django (Motor IA en la Nube)
  AI_BASE_URL: `http://${IA_IP}:${IA_PORT}`,
  AI_API_URL: `http://${IA_IP}:${IA_PORT}/api/predict/`,
  AI_REEL_URL: `http://${IA_IP}:${IA_PORT}/api/generar-reel/`,
  AI_TRANSLATE_URL: `http://${IA_IP}:${IA_PORT}/api/traducir-imagen/`,
};
