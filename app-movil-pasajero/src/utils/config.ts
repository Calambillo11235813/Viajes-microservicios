/**
 * Configuración Global de la Aplicación
 *
 * Centraliza la dirección IP y puertos para los diferentes microservicios.
 * Si pruebas desde un dispositivo físico (Android/iOS), cambia LOCAL_IP
 * por la dirección IP de tu computadora en la red local.
 */

// Cambia esta IP por la de tu computadora
const LOCAL_IP = '192.168.0.5';

export const CONFIG = {
  // Microservicio A: Spring Boot (GraphQL - Transaccional)
  GRAPHQL_URL: `http://${LOCAL_IP}:9090/graphql`,

  // Microservicio B: Django (Motor IA)
  AI_BASE_URL: `http://${LOCAL_IP}:8000`,
  AI_API_URL: `http://${LOCAL_IP}:8000/api/predict/`,
  AI_REEL_URL: `http://${LOCAL_IP}:8000/api/generar-reel/`,
  AI_TRANSLATE_URL: `http://${LOCAL_IP}:8000/api/traducir-imagen/`,
};
