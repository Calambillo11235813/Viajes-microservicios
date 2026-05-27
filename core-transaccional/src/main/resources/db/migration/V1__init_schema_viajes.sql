-- ============================================================
--  CORE TRANSACCIONAL - Agencia de Viajes Interdepartamental
--  Grupo 18 | Ingenieria de Software II | UAGRM
--  Base de Datos: PostgreSQL
-- ============================================================

-- ============================================================
-- 1. ROL
-- ============================================================
CREATE TABLE ROL (
    id_rol   SERIAL       PRIMARY KEY,
    nombre   VARCHAR(50)  NOT NULL UNIQUE
);

-- ============================================================
-- 2. USUARIO
-- ============================================================
CREATE TABLE USUARIO (
    id_usuario      SERIAL        PRIMARY KEY,
    ci_pasaporte    VARCHAR(20)   NOT NULL UNIQUE,
    nombre_completo VARCHAR(100)  NOT NULL,
    email           VARCHAR(100)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    telefono        VARCHAR(20),
    id_rol          INT           NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES ROL(id_rol)
);

-- ============================================================
-- 3. RUTA_DESTINO
-- ============================================================
CREATE TABLE RUTA_DESTINO (
    id_ruta                 SERIAL          PRIMARY KEY,
    ciudad_origen           VARCHAR(100)    NOT NULL,
    ciudad_destino          VARCHAR(100)    NOT NULL,
    duracion_estimada_horas DECIMAL(5, 2)   NOT NULL,
    categoria_turistica     VARCHAR(50),
    precio_base             DECIMAL(10, 2)  NOT NULL
);

-- ============================================================
-- 4. CATALOGO_CLIMA
-- ============================================================
CREATE TABLE CATALOGO_CLIMA (
    id_clima        SERIAL          PRIMARY KEY,
    id_ruta         INT             NOT NULL,
    mes             SMALLINT        NOT NULL CHECK (mes BETWEEN 1 AND 12),
    temporada       VARCHAR(30)     NOT NULL,
    temperatura_min DECIMAL(4, 1)   NOT NULL,
    temperatura_max DECIMAL(4, 1)   NOT NULL,
    descripcion     VARCHAR(200),
    recomendacion   VARCHAR(200),
    CONSTRAINT fk_clima_ruta FOREIGN KEY (id_ruta) REFERENCES RUTA_DESTINO(id_ruta)
);

-- ============================================================
-- 5. FLOTA
-- ============================================================
CREATE TABLE FLOTA (
    id_bus                   SERIAL       PRIMARY KEY,
    placa                    VARCHAR(20)  NOT NULL UNIQUE,
    capacidad_total_asientos INT          NOT NULL,
    tipo_bus                 VARCHAR(50)  NOT NULL
);

-- ============================================================
-- 6. VIAJE_PROGRAMADO
-- ============================================================
CREATE TABLE VIAJE_PROGRAMADO (
    id_viaje           SERIAL       PRIMARY KEY,
    id_ruta            INT          NOT NULL,
    id_bus             INT          NOT NULL,
    fecha_hora_salida  TIMESTAMP    NOT NULL,
    fecha_hora_llegada TIMESTAMP    NOT NULL,
    estado_viaje       VARCHAR(30)  NOT NULL DEFAULT 'PROGRAMADO',
                       -- PROGRAMADO | EN_CURSO | COMPLETADO | CANCELADO
    CONSTRAINT fk_viaje_ruta FOREIGN KEY (id_ruta) REFERENCES RUTA_DESTINO(id_ruta),
    CONSTRAINT fk_viaje_flota FOREIGN KEY (id_bus)  REFERENCES FLOTA(id_bus)
);

-- ============================================================
-- 7. HOTEL
-- ============================================================
CREATE TABLE HOTEL (
    id_hotel      SERIAL          PRIMARY KEY,
    nombre        VARCHAR(100)    NOT NULL,
    estrellas     SMALLINT        NOT NULL CHECK (estrellas BETWEEN 1 AND 5),
    precio_noche  DECIMAL(10, 2)  NOT NULL,
    ciudad        VARCHAR(100)    NOT NULL
);

-- ============================================================
-- 8. PAQUETE_TURISTICO
-- ============================================================
CREATE TABLE PAQUETE_TURISTICO (
    id_paquete     SERIAL          PRIMARY KEY,
    nombre_paquete VARCHAR(150)    NOT NULL,
    precio_total   DECIMAL(10, 2)  NOT NULL
);

-- ============================================================
-- 9. DETALLE_PAQUETE_HOTEL  (clase de asociacion M:M)
-- ============================================================
CREATE TABLE DETALLE_PAQUETE_HOTEL (
    id_detalle        SERIAL       PRIMARY KEY,
    id_paquete        INT          NOT NULL,
    id_hotel          INT          NOT NULL,
    duracion_estadia  INT          NOT NULL,   -- en noches
    tipo_habitacion   VARCHAR(50)  NOT NULL,
    cantidad_personas INT          NOT NULL,
    incluye_desayuno  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_detalle_paquete FOREIGN KEY (id_paquete) REFERENCES PAQUETE_TURISTICO(id_paquete),
    CONSTRAINT fk_detalle_hotel   FOREIGN KEY (id_hotel)   REFERENCES HOTEL(id_hotel),
    CONSTRAINT uq_paquete_hotel   UNIQUE (id_paquete, id_hotel)
);

-- ============================================================
-- 10. RESERVA
-- ============================================================
CREATE TABLE RESERVA (
    id_reserva         SERIAL          PRIMARY KEY,
    id_usuario         INT             NOT NULL,
    id_viaje           INT             NOT NULL,
    id_paquete         INT,                        -- NULL si no lleva paquete
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    estado_reserva     VARCHAR(30)     NOT NULL DEFAULT 'PENDIENTE',
                       -- PENDIENTE | CONFIRMADA | CANCELADA | COMPLETADA
    monto_total_pagado DECIMAL(10, 2)  NOT NULL DEFAULT 0,
    cantidad_pasajeros INT             NOT NULL,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario),
    CONSTRAINT fk_reserva_viaje   FOREIGN KEY (id_viaje)   REFERENCES VIAJE_PROGRAMADO(id_viaje),
    CONSTRAINT fk_reserva_paquete FOREIGN KEY (id_paquete) REFERENCES PAQUETE_TURISTICO(id_paquete)
);

-- ============================================================
-- 11. PAGO
-- ============================================================
CREATE TABLE PAGO (
    id_pago                  SERIAL          PRIMARY KEY,
    id_reserva               INT             NOT NULL UNIQUE,  -- 1:1 con RESERVA
    fecha_pago               TIMESTAMP       NOT NULL DEFAULT NOW(),
    monto_transaccion        DECIMAL(10, 2)  NOT NULL,
    metodo_pago_usado        VARCHAR(50)     NOT NULL,
                             -- QR | TRANSFERENCIA | BILLETERA_DIGITAL | EFECTIVO
    cupon_descuento_aplicado VARCHAR(50),
    estado_pago              VARCHAR(30)     NOT NULL DEFAULT 'PENDIENTE',
                             -- PENDIENTE | CONFIRMADO | RECHAZADO | REEMBOLSADO
    CONSTRAINT fk_pago_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVA(id_reserva)
);

-- ============================================================
-- 12. BOLETO_ASIENTO
-- ============================================================
CREATE TABLE BOLETO_ASIENTO (
    id_boleto       SERIAL       PRIMARY KEY,
    id_reserva      INT          NOT NULL,
    numero_asiento  VARCHAR(10)  NOT NULL,
    nombre_pasajero VARCHAR(100) NOT NULL,
    hash_blockchain VARCHAR(255) UNIQUE,        -- generado por Microservicio C
    codigo_qr       VARCHAR(255) UNIQUE,        -- generado al emitir
    fecha_emision   TIMESTAMP    NOT NULL DEFAULT NOW(),
    estado_boleto   VARCHAR(30)  NOT NULL DEFAULT 'EMITIDO',
                    -- EMITIDO | USADO | ANULADO
    tipo_pasajero   VARCHAR(30)  NOT NULL,
                    -- ADULTO | MENOR | TERCERA_EDAD
    CONSTRAINT fk_boleto_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVA(id_reserva)
);

-- ============================================================
-- INDICES - mejoran el rendimiento de las consultas frecuentes
-- ============================================================
CREATE INDEX idx_usuario_email        ON USUARIO(email);
CREATE INDEX idx_reserva_usuario      ON RESERVA(id_usuario);
CREATE INDEX idx_reserva_viaje        ON RESERVA(id_viaje);
CREATE INDEX idx_reserva_estado       ON RESERVA(estado_reserva);
CREATE INDEX idx_viaje_ruta           ON VIAJE_PROGRAMADO(id_ruta);
CREATE INDEX idx_viaje_fecha          ON VIAJE_PROGRAMADO(fecha_hora_salida);
CREATE INDEX idx_boleto_reserva       ON BOLETO_ASIENTO(id_reserva);
CREATE INDEX idx_clima_ruta_mes       ON CATALOGO_CLIMA(id_ruta, mes);
CREATE INDEX idx_pago_estado          ON PAGO(estado_pago);
