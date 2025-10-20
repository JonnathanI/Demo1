-- ==========================================================
-- 1. TABLA AVATAR_STYLES (CATÁLOGO DE LA TIENDA)
-- ==========================================================
-- Almacena todos los estilos de avatar disponibles y su precio.

CREATE TABLE IF NOT EXISTS AVATAR_STYLES (
                                             id SERIAL PRIMARY KEY,

    -- Nombre del ícono que usará el frontend (ej: 'FaRobot', 'FaFire')
                                             icon_name VARCHAR(50) UNIQUE NOT NULL,

    -- Precio en puntos del juego
                                             price INT NOT NULL CHECK (price >= 0),

    -- Si es un estilo que se debe comprar (TRUE) o es un estilo base/gratuito (FALSE)
                                             is_purchasable BOOLEAN NOT NULL DEFAULT TRUE
);

-- ==========================================================
-- 2. TABLA USER_AVATAR_STYLES (REGISTRO DE PROPIEDAD)
-- ==========================================================
-- Tabla de unión que registra qué estilos de avatar posee cada usuario.

CREATE TABLE IF NOT EXISTS USER_AVATAR_STYLES (
                                                  id SERIAL PRIMARY KEY,

    -- Clave foránea al usuario
                                                  user_id BIGINT NOT NULL,

    -- Clave foránea al estilo de avatar
                                                  style_id BIGINT NOT NULL,

    -- Restricción: Un usuario solo puede poseer el mismo estilo una vez
                                                  CONSTRAINT uc_user_style UNIQUE (user_id, style_id),

    -- Definición de las claves foráneas
                                                  CONSTRAINT fk_user_styles
                                                      FOREIGN KEY (user_id)
                                                          REFERENCES USERS (id)
                                                          ON DELETE CASCADE, -- Si el usuario se elimina, se eliminan sus estilos

                                                  CONSTRAINT fk_style_owner
                                                      FOREIGN KEY (style_id)
                                                          REFERENCES AVATAR_STYLES (id)
                                                          ON DELETE CASCADE -- Si el estilo se elimina, se elimina el registro de propiedad
);