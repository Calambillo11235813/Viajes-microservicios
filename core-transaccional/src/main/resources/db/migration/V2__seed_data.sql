-- ============================================================
--  DATOS SEMILLA - Core Transaccional
-- ============================================================

-- Roles
INSERT INTO ROL (nombre) VALUES ('CLIENTE'), ('ADMINISTRADOR'), ('GERENTE');

-- Rutas
INSERT INTO RUTA_DESTINO (ciudad_origen, ciudad_destino, duracion_estimada_horas, categoria_turistica, precio_base)
VALUES
    ('Santa Cruz', 'Cochabamba', 8.5,  'Cultural',  120.00),
    ('Santa Cruz', 'Sucre',      10.0, 'Historico', 150.00),
    ('Santa Cruz', 'La Paz',     14.0, 'Andino',    180.00);

-- Catalogo clima para ruta 1 (Santa Cruz -> Cochabamba)
INSERT INTO CATALOGO_CLIMA (id_ruta, mes, temporada, temperatura_min, temperatura_max, descripcion, recomendacion)
VALUES
    (1, 6,  'Invierno',   4.0, 18.0, 'Frio seco por la noche',    'Lleva abrigo'),
    (1, 7,  'Invierno',   3.0, 17.0, 'Noche muy fria',            'Ropa de abrigo gruesa'),
    (1, 11, 'Primavera', 12.0, 24.0, 'Agradable y soleado',       'Ropa ligera'),
    (1, 1,  'Verano',    15.0, 28.0, 'Caluroso con lluvias',      'Impermeable');

-- Flota
INSERT INTO FLOTA (placa, capacidad_total_asientos, tipo_bus)
VALUES
    ('SCZ-1234', 40, 'Semi-cama'),
    ('SCZ-5678', 50, 'Cama'),
    ('SCZ-9012', 45, 'Ejecutivo');

-- Viaje programado
INSERT INTO VIAJE_PROGRAMADO (id_ruta, id_bus, fecha_hora_salida, fecha_hora_llegada, estado_viaje)
VALUES
    (1, 1, '2026-07-15 08:00:00', '2026-07-15 16:30:00', 'PROGRAMADO'),
    (2, 2, '2026-07-16 20:00:00', '2026-07-17 06:00:00', 'PROGRAMADO');

-- Usuario de prueba
INSERT INTO USUARIO (ci_pasaporte, nombre_completo, email, password_hash, telefono, id_rol)
VALUES ('12345678', 'Carlos Perez', 'carlos@email.com', 'hash_seguro_aqui', '76543210', 1);

-- Hotel y paquete turistico
INSERT INTO HOTEL (nombre, estrellas, precio_noche, ciudad)
VALUES ('Hotel Colonial Sucre', 3, 180.00, 'Sucre');

INSERT INTO PAQUETE_TURISTICO (nombre_paquete, precio_total)
VALUES ('Sucre Colonial 3 dias', 350.00);

INSERT INTO DETALLE_PAQUETE_HOTEL (id_paquete, id_hotel, duracion_estadia, tipo_habitacion, cantidad_personas, incluye_desayuno)
VALUES (1, 1, 3, 'Doble', 2, TRUE);

-- Reserva de ejemplo (Carlos compra el paquete)
INSERT INTO RESERVA (id_usuario, id_viaje, id_paquete, estado_reserva, monto_total_pagado, cantidad_pasajeros)
VALUES (1, 2, 1, 'CONFIRMADA', 350.00, 1);

-- Pago
INSERT INTO PAGO (id_reserva, monto_transaccion, metodo_pago_usado, estado_pago)
VALUES (1, 350.00, 'QR', 'CONFIRMADO');

-- Boleto
INSERT INTO BOLETO_ASIENTO (id_reserva, numero_asiento, nombre_pasajero, codigo_qr, estado_boleto, tipo_pasajero)
VALUES (1, '12A', 'Carlos Perez', 'QR-5501-UNIQUE', 'EMITIDO', 'ADULTO');
