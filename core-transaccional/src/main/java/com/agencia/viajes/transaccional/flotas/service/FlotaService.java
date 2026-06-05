package com.agencia.viajes.transaccional.flotas.service;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.flotas.repository.FlotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlotaService {

    private final FlotaRepository flotaRepository;

    public List<Flota> listarFlotas() {
        return flotaRepository.findAll();
    }

    public Flota crearFlota(String placa, Integer capacidadTotalAsientos, String tipoBus) {
        Flota flota = new Flota();
        flota.setPlaca(placa);
        flota.setCapacidadTotalAsientos(capacidadTotalAsientos);
        flota.setTipoBus(tipoBus);
        return flotaRepository.save(flota);
    }

    public Flota actualizarFlota(Integer idBus, String placa, Integer capacidadTotalAsientos, String tipoBus) {
        Flota flota = flotaRepository.findById(idBus)
                .orElseThrow(() -> new IllegalArgumentException("Flota no encontrada"));

        if (placa != null) {
            flota.setPlaca(placa);
        }
        if (capacidadTotalAsientos != null) {
            flota.setCapacidadTotalAsientos(capacidadTotalAsientos);
        }
        if (tipoBus != null) {
            flota.setTipoBus(tipoBus);
        }

        return flotaRepository.save(flota);
    }

    public boolean eliminarFlota(Integer idBus) {
        if (!flotaRepository.existsById(idBus)) {
            return false;
        }
        flotaRepository.deleteById(idBus);
        return true;
    }
}
