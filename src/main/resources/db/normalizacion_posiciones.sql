-- Script para migrar posiciones a una tabla separada

-- 1. Crear tabla posiciones
CREATE TABLE posiciones (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    posicion_x NUMBER DEFAULT 250 NOT NULL,
    posicion_y NUMBER DEFAULT 250 NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Agregar columna posicion_id a elementos_texto
ALTER TABLE elementos_texto ADD posicion_id NUMBER;

-- 3. Agregar columna posicion_id a elementos_imagen
ALTER TABLE elementos_imagen ADD posicion_id NUMBER;

-- 4. Migrar datos de elementos_texto a posiciones
DECLARE
    new_posicion_id NUMBER;
BEGIN
    FOR elemento_texto_rec IN (SELECT id, posicion_x, posicion_y FROM elementos_texto) LOOP
        INSERT INTO posiciones (posicion_x, posicion_y) 
        VALUES (elemento_texto_rec.posicion_x, elemento_texto_rec.posicion_y)
        RETURNING id INTO new_posicion_id;
        
        UPDATE elementos_texto 
        SET posicion_id = new_posicion_id
        WHERE id = elemento_texto_rec.id;
    END LOOP;
    COMMIT;
END;
/

-- 5. Migrar datos de elementos_imagen a posiciones
DECLARE
    new_posicion_id NUMBER;
BEGIN
    FOR elemento_imagen_rec IN (SELECT id, posicion_x, posicion_y FROM elementos_imagen) LOOP
        INSERT INTO posiciones (posicion_x, posicion_y) 
        VALUES (elemento_imagen_rec.posicion_x, elemento_imagen_rec.posicion_y)
        RETURNING id INTO new_posicion_id;
        
        UPDATE elementos_imagen 
        SET posicion_id = new_posicion_id
        WHERE id = elemento_imagen_rec.id;
    END LOOP;
    COMMIT;
END;
/

-- 6. Hacer no nulable la columna posicion_id en elementos_texto
ALTER TABLE elementos_texto MODIFY posicion_id NUMBER NOT NULL;

-- 7. Hacer no nulable la columna posicion_id en elementos_imagen
ALTER TABLE elementos_imagen MODIFY posicion_id NUMBER NOT NULL;

-- 8. Agregar restricciones de clave foránea
ALTER TABLE elementos_texto ADD CONSTRAINT fk_elementos_texto_posicion FOREIGN KEY (posicion_id) REFERENCES posiciones(id);
ALTER TABLE elementos_imagen ADD CONSTRAINT fk_elementos_imagen_posicion FOREIGN KEY (posicion_id) REFERENCES posiciones(id);

-- 9. Eliminar columnas redundantes
ALTER TABLE elementos_texto DROP COLUMN posicion_x;
ALTER TABLE elementos_texto DROP COLUMN posicion_y;
ALTER TABLE elementos_imagen DROP COLUMN posicion_x;
ALTER TABLE elementos_imagen DROP COLUMN posicion_y;

COMMIT;
