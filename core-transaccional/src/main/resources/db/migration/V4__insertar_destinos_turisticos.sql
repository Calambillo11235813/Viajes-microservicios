-- ============================================================
--  V4 - SEED DATA DESTINOS TURISTICOS
-- ============================================================

INSERT INTO DESTINO_TURISTICO (nombre_turistico, departamento, descripcion) VALUES
    ('Uyuni', 'Potosi', 'Salar de Uyuni, el mayor desierto de sal continuo y alto del mundo.'),
    ('Samaipata', 'Santa Cruz', 'Conocido por El Fuerte de Samaipata, un sitio arqueológico precolombino.'),
    ('Cristo_ConCordia', 'Cochabamba', 'Estatua monumental de Jesucristo ubicada en el cerro San Pedro.');

-- Ajustar la secuencia si es necesario
SELECT setval(pg_get_serial_sequence('DESTINO_TURISTICO', 'id_destino'), COALESCE((SELECT MAX(id_destino) FROM DESTINO_TURISTICO), 1), true);
