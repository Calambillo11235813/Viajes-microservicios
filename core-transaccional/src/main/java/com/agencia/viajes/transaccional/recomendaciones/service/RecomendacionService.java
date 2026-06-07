package com.agencia.viajes.transaccional.recomendaciones.service;

import com.agencia.viajes.transaccional.recomendaciones.dto.MotorIaResponse;
import com.agencia.viajes.transaccional.recomendaciones.dto.RecomendacionRutaResponse;
import com.agencia.viajes.transaccional.recomendaciones.dto.TopRuta;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * Servicio de recomendación personalizada de rutas (CU-09).
 *
 * <p>Calcula el perfil del pasajero a partir de percentiles globales de gasto,
 * determina su categoría turística preferida y orquesta la llamada REST al motor
 * IA (microservicio Django) para obtener la ruta recomendada. Ante cualquier fallo
 * de comunicación devuelve una respuesta por defecto para no romper la experiencia
 * del cliente.</p>
 */
@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private static final Logger log = LoggerFactory.getLogger(RecomendacionService.class);

    /** Perfiles posibles del pasajero según su gasto histórico. */
    private static final String PERFIL_ECONOMICO = "Económico";
    private static final String PERFIL_ESTANDAR = "Estándar";
    private static final String PERFIL_PREMIUM = "Premium";

    /** Categoría usada cuando el usuario no tiene historial de reservas. */
    private static final String CATEGORIA_DESCONOCIDA = "Desconocido";

    /** Ruta devuelta por defecto cuando el motor IA no responde. */
    private static final int RUTA_POR_DEFECTO = 1;

    /** Cantidad de pasajeros fija para esta versión del CU-09. */
    private static final int CANTIDAD_PASAJEROS = 1;

    /** Percentiles que delimitan los tramos de perfil. */
    private static final double PERCENTIL_INFERIOR = 33.0;
    private static final double PERCENTIL_SUPERIOR = 67.0;

    private final ReservaRepository reservaRepository;
    private final RestClient motorIaRestClient;
    private final ObjectMapper objectMapper;

    @Value("${motor-ia.recomendacion-path}")
    private String motorIaRecomendacionPath;

    /** Umbral P33 del gasto global; por debajo se considera perfil Económico. */
    private volatile double umbralEconomico;

    /** Umbral P67 del gasto global; por encima se considera perfil Premium. */
    private volatile double umbralPremium;

    /**
     * Calcula una sola vez, al iniciar el servicio, los umbrales de perfil a partir
     * de los percentiles 33 y 67 del gasto total de todos los usuarios. Si no hay
     * datos suficientes, los umbrales quedan en cero (todos serían al menos Estándar
     * salvo gasto cero, que cae en Económico).
     */
    @PostConstruct
    public void calcularPercentilesGlobales() {
        try {
            List<Double> gastos = reservaRepository.obtenerGastosTotalesPorUsuario().stream()
                    .filter(g -> g != null)
                    .map(BigDecimal::doubleValue)
                    .sorted()
                    .toList();

            if (gastos.isEmpty()) {
                this.umbralEconomico = 0.0;
                this.umbralPremium = 0.0;
                log.warn("[CU-09] Sin datos de gasto; umbrales de perfil en 0.");
                return;
            }

            this.umbralEconomico = calcularPercentil(gastos, PERCENTIL_INFERIOR);
            this.umbralPremium = calcularPercentil(gastos, PERCENTIL_SUPERIOR);
            log.info("[CU-09] Umbrales de perfil calculados -> Económico<={}, Premium>={}",
                    umbralEconomico, umbralPremium);
        } catch (Exception e) {
            this.umbralEconomico = 0.0;
            this.umbralPremium = 0.0;
            log.error("[CU-09] Error al calcular percentiles globales: {}", e.getMessage());
        }
    }

    /**
     * Calcula un percentil sobre una lista YA ordenada ascendentemente usando
     * interpolación lineal entre rangos.
     *
     * @param valoresOrdenados lista de valores ordenada de menor a mayor (no vacía).
     * @param percentil percentil deseado en el rango 0-100.
     * @return valor del percentil solicitado.
     */
    private double calcularPercentil(List<Double> valoresOrdenados, double percentil) {
        int n = valoresOrdenados.size();
        if (n == 1) {
            return valoresOrdenados.get(0);
        }
        double rango = (percentil / 100.0) * (n - 1);
        int indiceInferior = (int) Math.floor(rango);
        int indiceSuperior = (int) Math.ceil(rango);
        double fraccion = rango - indiceInferior;

        double valorInferior = valoresOrdenados.get(indiceInferior);
        double valorSuperior = valoresOrdenados.get(indiceSuperior);
        return valorInferior + (valorSuperior - valorInferior) * fraccion;
    }

    /**
     * Determina el perfil del pasajero comparando su gasto histórico con los
     * umbrales globales precalculados.
     *
     * @param idUsuario identificador del usuario.
     * @return {@code Económico}, {@code Estándar} o {@code Premium}; {@code Económico} si no hay datos.
     */
    @Transactional(readOnly = true)
    public String obtenerPerfilUsuario(Integer idUsuario) {
        BigDecimal gasto = reservaRepository.sumarGastoTotalUsuario(idUsuario);
        if (gasto == null || gasto.compareTo(BigDecimal.ZERO) <= 0) {
            return PERFIL_ECONOMICO;
        }
        double gastoUsuario = gasto.doubleValue();
        if (gastoUsuario <= umbralEconomico) {
            return PERFIL_ECONOMICO;
        }
        if (gastoUsuario >= umbralPremium) {
            return PERFIL_PREMIUM;
        }
        return PERFIL_ESTANDAR;
    }

    /**
     * Obtiene la categoría turística que el usuario ha reservado con mayor
     * frecuencia.
     *
     * @param idUsuario identificador del usuario.
     * @return categoría preferida o {@code Desconocido} si no tiene reservas válidas.
     */
    @Transactional(readOnly = true)
    public String obtenerCategoriaPreferida(Integer idUsuario) {
        List<String> categorias = reservaRepository.obtenerCategoriasPorFrecuencia(
                idUsuario, PageRequest.of(0, 1));
        if (categorias == null || categorias.isEmpty()) {
            return CATEGORIA_DESCONOCIDA;
        }
        return categorias.get(0);
    }

    /**
     * Orquesta la recomendación de ruta para un usuario: calcula perfil y categoría,
     * invoca al motor IA y construye la respuesta. Si el presupuesto no se indica,
     * usa el gasto histórico del usuario. Ante un fallo del motor IA devuelve una
     * recomendación por defecto con un mensaje de advertencia.
     *
     * @param idUsuario identificador del usuario (obligatorio).
     * @param presupuesto presupuesto opcional; si es nulo se usa el gasto histórico.
     * @return recomendación de ruta con perfil, categoría y top de rutas.
     * @throws IllegalArgumentException si {@code idUsuario} es nulo.
     */
    @Transactional(readOnly = true)
    public RecomendacionRutaResponse recomendarRuta(Integer idUsuario, Double presupuesto) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio.");
        }

        String perfil = obtenerPerfilUsuario(idUsuario);
        String categoria = obtenerCategoriaPreferida(idUsuario);
        double monto = resolverPresupuesto(idUsuario, presupuesto);

        Map<String, Object> cuerpoPeticion = new LinkedHashMap<>();
        cuerpoPeticion.put("perfil_pasajero", perfil);
        cuerpoPeticion.put("categoria_preferida", categoria);
        cuerpoPeticion.put("monto_total_pagado", monto);
        cuerpoPeticion.put("cantidad_pasajeros", CANTIDAD_PASAJEROS);

        try {
            String jsonBody = objectMapper.writeValueAsString(cuerpoPeticion);
            log.debug("[CU-09] Petición al motor IA: {}", jsonBody);

            MotorIaResponse respuesta = motorIaRestClient.post()
                    .uri(motorIaRecomendacionPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(MotorIaResponse.class);

            if (respuesta == null || respuesta.prediccionIdRuta() == null) {
                return respuestaPorDefecto(perfil, categoria,
                        "El motor IA devolvió una respuesta vacía; se usa la ruta por defecto.");
            }

            return new RecomendacionRutaResponse(
                    respuesta.prediccionIdRuta(),
                    perfil,
                    categoria,
                    mapearTopRutas(respuesta),
                    null);
        } catch (JsonProcessingException e) {
            log.error("[CU-09] Error al serializar la petición al motor IA: {}", e.getMessage());
            return respuestaPorDefecto(perfil, categoria,
                    "Error interno al preparar la petición al motor IA.");
        } catch (Exception e) {
            log.error("[CU-09] Fallo al consultar el motor IA: {}", e.getMessage());
            return respuestaPorDefecto(perfil, categoria,
                    "El motor IA no está disponible; se devuelve la ruta más popular por defecto.");
        }
    }

    /**
     * Resuelve el monto a enviar al motor IA: el presupuesto indicado o, en su
     * defecto, el gasto histórico del usuario.
     *
     * @param idUsuario identificador del usuario.
     * @param presupuesto presupuesto opcional recibido del cliente.
     * @return monto a utilizar como característica del modelo.
     */
    private double resolverPresupuesto(Integer idUsuario, Double presupuesto) {
        if (presupuesto != null && presupuesto > 0) {
            return presupuesto;
        }
        BigDecimal gasto = reservaRepository.sumarGastoTotalUsuario(idUsuario);
        return gasto != null ? gasto.doubleValue() : 0.0;
    }

    /**
     * Combina las listas de rutas y probabilidades del motor IA en objetos {@link TopRuta}.
     *
     * @param respuesta respuesta del motor IA.
     * @return lista de rutas candidatas con su probabilidad; vacía si no hay datos.
     */
    private List<TopRuta> mapearTopRutas(MotorIaResponse respuesta) {
        List<Integer> rutas = respuesta.top3Rutas();
        List<Double> probabilidades = respuesta.top3Probabilidades();
        if (rutas == null || rutas.isEmpty()) {
            return Collections.emptyList();
        }
        List<TopRuta> topRutas = new ArrayList<>();
        for (int i = 0; i < rutas.size(); i++) {
            Double probabilidad = (probabilidades != null && i < probabilidades.size())
                    ? probabilidades.get(i)
                    : null;
            topRutas.add(new TopRuta(rutas.get(i), probabilidad));
        }
        return topRutas;
    }

    /**
     * Construye la respuesta por defecto usada cuando el motor IA no está disponible
     * o responde de forma inválida.
     *
     * @param perfil perfil calculado del usuario.
     * @param categoria categoría preferida del usuario.
     * @param advertencia mensaje explicativo para el cliente.
     * @return recomendación por defecto con la ruta {@value #RUTA_POR_DEFECTO}.
     */
    private RecomendacionRutaResponse respuestaPorDefecto(
            String perfil, String categoria, String advertencia) {
        return new RecomendacionRutaResponse(
                RUTA_POR_DEFECTO,
                perfil,
                categoria,
                Collections.emptyList(),
                advertencia);
    }
}
