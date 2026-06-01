-- ============================================================
--  V2 - DATA SEEDING DEL CATALOGO
--  Core Transaccional - Agencia de Viajes Interdepartamental
--  Contexto: Bolivia
-- ============================================================

-- ============================================================
-- 1. ROL
-- ============================================================
INSERT INTO ROL (id_rol, nombre) VALUES
    (1, 'ADMINISTRADOR'),
    (2, 'CLIENTE'),
    (3, 'GERENTE');

-- ============================================================
-- 2. RUTA_DESTINO
-- ============================================================
INSERT INTO RUTA_DESTINO (
    id_ruta,
    ciudad_origen,
    ciudad_destino,
    duracion_estimada_horas,
    categoria_turistica,
    precio_base
) VALUES
    (1,  'Santa Cruz',  'Cochabamba',  9.00,  'Interdepartamental', 135.00),
    (2,  'Santa Cruz',  'Sucre',      11.50,  'Historico',          170.00),
    (3,  'Santa Cruz',  'La Paz',     17.00,  'Altiplano',          240.00),
    (4,  'Santa Cruz',  'Tarija',     15.00,  'Sur',                220.00),
    (5,  'Santa Cruz',  'Oruro',      16.50,  'Altiplano',          235.00),
    (6,  'La Paz',      'Cochabamba',  6.50,  'Valle',               95.00),
    (7,  'La Paz',      'Oruro',       3.50,  'Altiplano',           55.00),
    (8,  'La Paz',      'Sucre',      10.50,  'Historico',          145.00),
    (9,  'La Paz',      'Potosi',      9.00,  'Altiplano',          130.00),
    (10, 'Cochabamba',  'Sucre',       7.00,  'Valle',              100.00),
    (11, 'Cochabamba',  'Oruro',       5.00,  'Altiplano',           75.00),
    (12, 'Cochabamba',  'Tarija',     12.50,  'Sur',                175.00),
    (13, 'Oruro',       'Potosi',      4.00,  'Altiplano',           60.00),
    (14, 'Sucre',       'Potosi',      4.50,  'Colonial',            65.00),
    (15, 'Tarija',      'Sucre',      14.00,  'Sur',                195.00);

-- ============================================================
-- 3. FLOTA
-- ============================================================
INSERT INTO FLOTA (id_bus, placa, capacidad_total_asientos, tipo_bus) VALUES
    (1,  '1234-ABC', 40, 'Semi-Lecho'),
    (2,  '2456-DEF', 48, 'Lecho'),
    (3,  '3567-GHJ', 52, 'Normal'),
    (4,  '4678-KLM', 36, 'Cama'),
    (5,  '5789-NPQ', 45, 'Semi-Lecho'),
    (6,  '6890-RST', 55, 'Normal'),
    (7,  '7921-UVW', 42, 'Lecho'),
    (8,  '8462-XYZ', 50, 'Cama'),
    (9,  '9153-BCD', 38, 'Semi-Lecho'),
    (10, '2047-EFG', 46, 'Lecho');

-- ============================================================
-- 4. HOTEL
-- ============================================================
INSERT INTO HOTEL (id_hotel, nombre, estrellas, precio_noche, ciudad) VALUES
    (1,  'Hotel Casa Blanca Santa Cruz',      5, 620.00, 'Santa Cruz'),
    (2,  'Suites del Oriente',                 4, 480.00, 'Santa Cruz'),
    (3,  'Hotel Amazonico Plaza',             4, 390.00, 'Santa Cruz'),
    (4,  'Residencial Santa Cruz Centro',     3, 260.00, 'Santa Cruz'),
    (5,  'Hotel Brisas del Urubo',            5, 710.00, 'Santa Cruz'),
    (6,  'Hotel Illimani Plaza',              5, 590.00, 'La Paz'),
    (7,  'Hotel Altiplano Real',              4, 420.00, 'La Paz'),
    (8,  'Residencial Mirador de Sopocachi',   3, 250.00, 'La Paz'),
    (9,  'Hotel Condor Andino',                4, 360.00, 'La Paz'),
    (10, 'Hostal Centro Paceno',               3, 210.00, 'La Paz'),
    (11, 'Hotel Tunari Palace',                5, 520.00, 'Cochabamba'),
    (12, 'Hotel Jardines del Valle',           4, 340.00, 'Cochabamba'),
    (13, 'Residencial Colcapirhua',            3, 220.00, 'Cochabamba'),
    (14, 'Hotel Plaza del Valle',              4, 300.00, 'Cochabamba'),
    (15, 'Hotel Casa de la Libertad',          5, 460.00, 'Sucre'),
    (16, 'Hotel La Recoleta Colonial',         4, 350.00, 'Sucre'),
    (17, 'Hostal Siete Lunas',                 3, 190.00, 'Sucre'),
    (18, 'Hotel Blanco Centro',                3, 230.00, 'Sucre'),
    (19, 'Hotel Vinos del Sur',                5, 440.00, 'Tarija'),
    (20, 'Hotel Guadalquivir',                 4, 320.00, 'Tarija'),
    (21, 'Hostal Valle Dorado',                3, 200.00, 'Tarija'),
    (22, 'Hotel Carnaval Imperial',            4, 310.00, 'Oruro'),
    (23, 'Residencial del Socavon',            3, 180.00, 'Oruro'),
    (24, 'Hotel Cerro Rico Plaza',             4, 290.00, 'Potosi'),
    (25, 'Hostal Minero Colonial',             3, 170.00, 'Potosi');

-- ============================================================
-- 5. PAQUETE_TURISTICO
-- ============================================================
INSERT INTO PAQUETE_TURISTICO (id_paquete, nombre_paquete, precio_total) VALUES
    (1,  'Especial Carnaval de Oruro',        1490.00),
    (2,  'Finde de Invierno en el Salar',     1180.00),
    (3,  'Ruta del Vino en Tarija',           1320.00),
    (4,  'Escapada Colonial a Sucre',          980.00),
    (5,  'Altiplano y Miradores de La Paz',   1250.00),
    (6,  'Valle y Gastronomia Cochabamba',     890.00),
    (7,  'Crucena City Break y Urubo',        1100.00),
    (8,  'Bolivia Sur Cultural',              1450.00),
    (9,  'Mineria y Patrimonio de Potosi',    1020.00),
    (10, 'Circuito Interdepartamental Clasico', 1680.00);

-- ============================================================
-- 6. CATALOGO_CLIMA
-- ============================================================
INSERT INTO CATALOGO_CLIMA (
    id_ruta,
    mes,
    temporada,
    temperatura_min,
    temperatura_max,
    descripcion,
    recomendacion
) VALUES
    (1,  1, 'Verano',   22.0, 33.0, 'Calor humedo en la salida crucena',           'Hidratacion y ropa ligera'),
    (1,  7, 'Invierno',  7.0,  22.0, 'Madrugadas frescas en Cochabamba',            'Abrigo liviano para la noche'),
    (2,  1, 'Verano',   20.0, 34.0, 'Calor y lluvias en la llanura crucena',       'Llevar impermeable y agua'),
    (2,  7, 'Invierno',   8.0,  24.0, 'Noches frias en Sucre',                       'Abrigo moderado'),
    (3,  6, 'Invierno',   4.0,  18.0, 'Ingreso al altiplano con frio seco',         'Abrigo grueso'),
    (3, 12, 'Verano',    12.0,  24.0, 'Temporada de lluvias y sol intenso',         'Viajar de dia y usar protector solar'),
    (4,  1, 'Verano',    18.0,  32.0, 'Calor intenso en Tarija con tardes agradables', 'Protector solar y agua'),
    (4,  7, 'Invierno',   6.0,  20.0, 'Noches frescas en el sur boliviano',         'Chaqueta ligera'),
    (5,  6, 'Invierno',   2.0,  16.0, 'Frio y viento en la ruta altiplanica',       'Abrigo grueso y guantes'),
    (6,  7, 'Invierno',   1.0,  16.0, 'Altiplano frio antes de llegar a Oruro',     'Manta y ropa termica'),
    (7,  6, 'Invierno',  -2.0,  17.0, 'Frio seco y altura elevada',                 'Abrigo termico'),
    (7, 12, 'Verano',     6.0,  20.0, 'Cielos despejados y radiacion alta',         'Lentes de sol y bloqueador'),
    (8,  6, 'Invierno',   0.0,  16.0, 'Noches muy frias en Sucre',                  'Abrigo y bebida caliente'),
    (9,  7, 'Invierno',  -3.0,  14.0, 'Altura, viento y sensacion termica baja',    'Ropa termica'),
    (10, 1, 'Verano',    12.0,  26.0, 'Clima templado con lluvias aisladas',        'Paraguas y calzado cerrado'),
    (11, 7, 'Invierno',  -1.0,  15.0, 'Frio seco rumbo a Oruro',                    'Abrigo grueso'),
    (12, 1, 'Verano',    15.0,  30.0, 'Valle calido hacia el sur',                  'Agua y gorra'),
    (12, 7, 'Invierno',   4.0,  18.0, 'Noche fresca en el trayecto',                'Abrigo moderado'),
    (13, 6, 'Invierno',  -4.0,  13.0, 'Frio intenso en el altiplano minero',        'Abrigo termico'),
    (14, 1, 'Verano',    10.0,  24.0, 'Dias templados y noches frias',              'Abrigo ligero'),
    (15, 1, 'Verano',    16.0,  31.0, 'Sur templado con lluvias ocasionales',       'Impermeable'),
    (15, 7, 'Invierno',   5.0,  19.0, 'Noches frescas en el sur',                   'Chaqueta ligera');

-- ============================================================
-- 7. DETALLE_PAQUETE_HOTEL
-- ============================================================
INSERT INTO DETALLE_PAQUETE_HOTEL (
    id_paquete,
    id_hotel,
    duracion_estadia,
    tipo_habitacion,
    cantidad_personas,
    incluye_desayuno
) VALUES
    (1,  22, 3, 'Doble',      2, TRUE),
    (2,  24, 2, 'Doble',      2, TRUE),
    (3,  19, 2, 'Suite',      2, TRUE),
    (3,  20, 1, 'Doble',      2, FALSE),
    (4,  15, 2, 'Doble',      2, TRUE),
    (4,  16, 1, 'Simple',     1, TRUE),
    (5,   6, 2, 'Suite',      2, TRUE),
    (5,   7, 1, 'Doble',      2, TRUE),
    (6,  11, 2, 'Doble',      2, TRUE),
    (6,  14, 1, 'Simple',     1, FALSE),
    (7,   1, 2, 'Suite',      2, TRUE),
    (7,   5, 1, 'Doble',      2, TRUE),
    (8,  19, 2, 'Suite',      2, TRUE),
    (8,  16, 1, 'Doble',      2, TRUE),
    (9,  24, 2, 'Doble',      2, TRUE),
    (9,  25, 1, 'Simple',     1, FALSE),
    (10, 1, 1, 'Suite',      2, TRUE),
    (10, 6, 1, 'Suite',      2, TRUE),
    (10, 11, 1, 'Suite',     2, TRUE),
    (10, 15, 1, 'Suite',     2, TRUE);

-- ============================================================
-- 8. AJUSTE DE SECUENCIAS
-- ============================================================
SELECT setval(pg_get_serial_sequence('ROL', 'id_rol'), COALESCE((SELECT MAX(id_rol) FROM ROL), 1), true);
SELECT setval(pg_get_serial_sequence('RUTA_DESTINO', 'id_ruta'), COALESCE((SELECT MAX(id_ruta) FROM RUTA_DESTINO), 1), true);
SELECT setval(pg_get_serial_sequence('FLOTA', 'id_bus'), COALESCE((SELECT MAX(id_bus) FROM FLOTA), 1), true);
SELECT setval(pg_get_serial_sequence('HOTEL', 'id_hotel'), COALESCE((SELECT MAX(id_hotel) FROM HOTEL), 1), true);
SELECT setval(pg_get_serial_sequence('PAQUETE_TURISTICO', 'id_paquete'), COALESCE((SELECT MAX(id_paquete) FROM PAQUETE_TURISTICO), 1), true);
SELECT setval(pg_get_serial_sequence('CATALOGO_CLIMA', 'id_clima'), COALESCE((SELECT MAX(id_clima) FROM CATALOGO_CLIMA), 1), true);
SELECT setval(pg_get_serial_sequence('DETALLE_PAQUETE_HOTEL', 'id_detalle'), COALESCE((SELECT MAX(id_detalle) FROM DETALLE_PAQUETE_HOTEL), 1), true);