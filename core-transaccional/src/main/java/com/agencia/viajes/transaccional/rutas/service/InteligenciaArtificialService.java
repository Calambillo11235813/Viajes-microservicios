package com.agencia.viajes.transaccional.rutas.service;

import com.agencia.viajes.transaccional.rutas.dto.DestinoRecomendadoResponse;
import com.agencia.viajes.transaccional.rutas.dto.ReelTuristicoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteligenciaArtificialService {

    public List<DestinoRecomendadoResponse> buscarDestinosPorImagen(String urlImagen) {
        // MOCK data (Read-Only) que simula la respuesta de un Microservicio de IA
        return List.of(
                new DestinoRecomendadoResponse("Uyuni, Bolivia", 95.5, "El salar más grande del mundo, similar a tu imagen."),
                new DestinoRecomendadoResponse("Lago Titicaca, Bolivia", 80.2, "Un destino hermoso con paisajes similares.")
        );
    }

    public ReelTuristicoResponse generarReelTuristico(Integer idRuta) {
        // MOCK data
        return new ReelTuristicoResponse("https://viajes.com/reels/ruta-" + idRuta + ".mp4", 30);
    }
}
