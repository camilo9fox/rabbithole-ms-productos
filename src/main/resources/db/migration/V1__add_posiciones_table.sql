-- 1. Crear nueva tabla de posiciones
CREATE TABLE posiciones (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    posicion_x NUMBER DEFAULT 250 NOT NULL,
    posicion_y NUMBER DEFAULT 250 NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Crear columna para referencias en ambas tablas
ALTER TABLE elementos_texto ADD posicion_id NUMBER;
ALTER TABLE elementos_imagen ADD posicion_id NUMBER;

-- 3. Migrar datos existentes a la nueva tabla
-- Para elementos_texto
MERGE INTO posiciones p
USING (SELECT id, posicion_x, posicion_y FROM elementos_texto) et
ON (1=0)
WHEN NOT MATCHED THEN
    INSERT (posicion_x, posicion_y)
    VALUES (et.posicion_x, et.posicion_y)
    RETURNING id INTO et.posicion_id;

-- Actualizar referencias en elementos_texto
UPDATE elementos_texto et
SET posicion_id = (
    SELECT MAX(p.id)
    FROM posiciones p
    WHERE p.posicion_x = et.posicion_x AND p.posicion_y = et.posicion_y
);

-- Para elementos_imagen
MERGE INTO posiciones p
USING (SELECT id, posicion_x, posicion_y FROM elementos_imagen) ei
ON (1=0)
WHEN NOT MATCHED THEN
    INSERT (posicion_x, posicion_y)
    VALUES (ei.posicion_x, ei.posicion_y)
    RETURNING id INTO ei.posicion_id;

-- Actualizar referencias en elementos_imagen
UPDATE elementos_imagen ei
SET posicion_id = (
    SELECT MAX(p.id)
    FROM posiciones p
    WHERE p.posicion_x = ei.posicion_x AND p.posicion_y = ei.posicion_y
);

-- 4. Configurar restricciones de clave externa y no nulo
ALTER TABLE elementos_texto MODIFY posicion_id NUMBER NOT NULL;
ALTER TABLE elementos_imagen MODIFY posicion_id NUMBER NOT NULL;

ALTER TABLE elementos_texto ADD CONSTRAINT fk_elementos_texto_posicion
    FOREIGN KEY (posicion_id) REFERENCES posiciones(id);

ALTER TABLE elementos_imagen ADD CONSTRAINT fk_elementos_imagen_posicion
    FOREIGN KEY (posicion_id) REFERENCES posiciones(id);

-- 5. Eliminar columnas redundantes
ALTER TABLE elementos_texto DROP COLUMN posicion_x;
ALTER TABLE elementos_texto DROP COLUMN posicion_y;

ALTER TABLE elementos_imagen DROP COLUMN posicion_x;
ALTER TABLE elementos_imagen DROP COLUMN posicion_y;

COMMIT;
