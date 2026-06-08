-- ============================================================
-- V5: Tablas para Módulo BI Gerencial (Motor IA CU10 y CU11)
-- ============================================================

-- 1. Tabla para historial de segmentación de usuarios (CU11)
CREATE TABLE USUARIO_CLUSTER_HISTORICO (
    id                  SERIAL          PRIMARY KEY,
    id_usuario          INT             NOT NULL,
    cluster_id          INT             NOT NULL,
    total_gastado       DECIMAL(12,2)   NOT NULL,
    num_reservas        INT             NOT NULL,
    rutas_distintas     INT             NOT NULL,
    promedio_pasajeros  DECIMAL(5,2)    NOT NULL,
    fecha_asignacion    TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cluster_usuario
        FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
);

CREATE INDEX idx_cluster_hist_usuario  ON USUARIO_CLUSTER_HISTORICO(id_usuario);
CREATE INDEX idx_cluster_hist_fecha    ON USUARIO_CLUSTER_HISTORICO(fecha_asignacion);
CREATE INDEX idx_cluster_hist_cluster  ON USUARIO_CLUSTER_HISTORICO(cluster_id);

-- 2. Tabla para caché de reglas de asociación (CU10)
CREATE TABLE REGLA_ASOCIACION_CACHE (
    id              SERIAL          PRIMARY KEY,
    antecedents     TEXT            NOT NULL,    -- JSON array de IDs de ruta
    consequents     TEXT            NOT NULL,    -- JSON array o ID único
    soporte         DECIMAL(8,6)    NOT NULL,
    confianza       DECIMAL(8,6)    NOT NULL,
    lift            DECIMAL(8,4)    NOT NULL,
    fecha_carga     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regla_lift       ON REGLA_ASOCIACION_CACHE(lift DESC);
CREATE INDEX idx_regla_fecha      ON REGLA_ASOCIACION_CACHE(fecha_carga);

-- 3. Tabla para snapshots de KPIs del Dashboard (Métricas Diarias)
CREATE TABLE DASHBOARD_KPI_SNAPSHOT (
    id                          SERIAL          PRIMARY KEY,
    fecha_snapshot              DATE            NOT NULL UNIQUE,
    total_usuarios              INT             NOT NULL,
    total_usuarios_segmentados  INT             NOT NULL,
    distribucion_clusters       JSONB           NOT NULL, -- {"0": 45, "1": 30, "2": 25}
    ingreso_por_cluster         JSONB           NOT NULL, -- {"0": 125000.50, "1": 78000, ...}
    conversion_por_cluster      JSONB           NOT NULL, -- {"0": 0.82, "1": 0.65, ...}
    total_reglas_asociacion     INT             NOT NULL,
    reglas_alto_lift_count      INT             NOT NULL,  -- lift > 1.2
    support_promedio_top20      DECIMAL(8,6),
    indice_cross_selling        INT             NOT NULL,  -- pares únicos con conf>0.5, lift>1.2
    fecha_creacion              TIMESTAMP       NOT NULL DEFAULT NOW()
);
