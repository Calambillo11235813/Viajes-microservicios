-- ============================================================
-- 13. DESTINO_TURISTICO
-- ============================================================
CREATE TABLE DESTINO_TURISTICO (
    id_destino       SERIAL       PRIMARY KEY,
    nombre_turistico VARCHAR(100) NOT NULL UNIQUE,
    departamento     VARCHAR(100) NOT NULL,
    descripcion      TEXT
);

CREATE INDEX idx_destino_turistico_nombre ON DESTINO_TURISTICO(nombre_turistico);
