CREATE TABLE NOTIFICACION (
    id_notificacion SERIAL PRIMARY KEY,
    id_usuario      INTEGER      NOT NULL REFERENCES USUARIO(id_usuario),
    tipo            VARCHAR(40)  NOT NULL,
    titulo          VARCHAR(120) NOT NULL,
    mensaje         VARCHAR(500) NOT NULL,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leido           BOOLEAN      NOT NULL DEFAULT FALSE,
    datos_extra_json TEXT
);

CREATE INDEX idx_notificacion_usuario_fecha ON NOTIFICACION (id_usuario, fecha_creacion DESC);
CREATE INDEX idx_notificacion_usuario_leido ON NOTIFICACION (id_usuario, leido);

CREATE TABLE DISPOSITIVO_PUSH (
    id_dispositivo           SERIAL PRIMARY KEY,
    id_usuario               INTEGER      NOT NULL REFERENCES USUARIO(id_usuario),
    token                    VARCHAR(255) NOT NULL,
    plataforma               VARCHAR(20)  NOT NULL,
    activo                   BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dispositivo_push_token UNIQUE (token)
);

CREATE INDEX idx_dispositivo_push_usuario ON DISPOSITIVO_PUSH (id_usuario, activo);
