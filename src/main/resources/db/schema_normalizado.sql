BEGIN
   FOR cur_rec IN (SELECT object_name, object_type
                   FROM user_objects
                   WHERE object_type IN ('TABLE','VIEW')
                   ORDER BY object_type)
   LOOP
      BEGIN
         IF cur_rec.object_type = 'TABLE' THEN
            EXECUTE IMMEDIATE 'DROP TABLE "' || cur_rec.object_name || '" CASCADE CONSTRAINTS PURGE';
         ELSE
            EXECUTE IMMEDIATE 'DROP VIEW "' || cur_rec.object_name || '"';
         END IF;
      EXCEPTION
         WHEN OTHERS THEN NULL;
      END;
   END LOOP;
END;
/

-- 1. Usuarios
CREATE TABLE usuarios (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR2(100) NOT NULL UNIQUE,
    nombre VARCHAR2(100) NOT NULL,
    apellido VARCHAR2(100),
    oid VARCHAR2(50) NOT NULL UNIQUE,
    telefono VARCHAR2(20),
    direccion VARCHAR2(200),
    ciudad VARCHAR2(100),
    estado VARCHAR2(100),
    pais VARCHAR2(100),
    codigo_postal VARCHAR2(20)
);

CREATE TABLE cloudinary_resources (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url_imagen CLOB NOT NULL,
    public_id VARCHAR2(255) NOT NULL,
    tipo_imagen VARCHAR2(50),
    nombre_archivo VARCHAR2(255),
    tamano_archivo NUMBER,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Colores
CREATE TABLE colores (
    id VARCHAR2(20) PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL,
    codigo_hex VARCHAR2(7) NOT NULL,
    texto_preview VARCHAR2(7) NOT NULL,
    precio_adicional NUMBER DEFAULT 0 NOT NULL
);

-- 3. Tallas
CREATE TABLE tallas (
    id VARCHAR2(10) PRIMARY KEY,
    nombre VARCHAR2(10) NOT NULL,
    precio_adicional NUMBER DEFAULT 0 NOT NULL
);

-- 4. Fuentes disponibles
CREATE TABLE fuentes (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL UNIQUE,
    url VARCHAR2(255)
);

-- 5. Categorías de productos
CREATE TABLE categorias (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL UNIQUE,
    descripcion CLOB
);

-- 6. Estados de diseño
CREATE TABLE estados_diseno (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR2(30) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL,
    descripcion CLOB
);

-- 7. Estados de orden
CREATE TABLE estados_orden (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR2(30) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL,
    descripcion CLOB
);

-- 8. Métodos de pago
CREATE TABLE metodos_pago (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR2(30) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL,
    activo NUMBER(1) DEFAULT 1 NOT NULL
);

-- 9. Tipos de ángulo
CREATE TABLE tipos_angulo (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR2(20) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL
);

-- 10. Tipos de ítem
CREATE TABLE tipos_item (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR2(20) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL
);

-- NUEVA TABLA: Posiciones
CREATE TABLE posiciones (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    posicion_x NUMBER DEFAULT 250 NOT NULL,
    posicion_y NUMBER DEFAULT 250 NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 11. Diseño Personalizado
CREATE TABLE disenos_personalizados (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id NUMBER REFERENCES usuarios(id) ON DELETE SET NULL,
    nombre VARCHAR2(255) DEFAULT 'Polera Personalizada',
    color_id VARCHAR2(20) NOT NULL REFERENCES colores(id),
    talla_id VARCHAR2(10) NOT NULL REFERENCES tallas(id),
    precio NUMBER(10, 2) NOT NULL,
    estado_id NUMBER NOT NULL REFERENCES estados_diseno(id),
    motivo_rechazo CLOB,
    notas_modificacion CLOB,
    creado_por_admin NUMBER(1) DEFAULT 0 NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 12. Elemento de Diseño (Texto)
CREATE TABLE elementos_texto (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    contenido CLOB NOT NULL,
    fuente_id NUMBER NOT NULL REFERENCES fuentes(id),
    color_id VARCHAR2(20) NOT NULL REFERENCES colores(id),
    tamanio NUMBER DEFAULT 30 NOT NULL,
    posicion_id NUMBER NOT NULL REFERENCES posiciones(id)
);

-- 13. Elemento de Diseño (Imagen)
CREATE TABLE elementos_imagen (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cloudinary_resource_id NUMBER NOT NULL REFERENCES cloudinary_resources(id) ON DELETE CASCADE,
    posicion_id NUMBER NOT NULL REFERENCES posiciones(id),
    ancho NUMBER DEFAULT 150 NOT NULL,
    alto NUMBER DEFAULT 150 NOT NULL
);

-- 14. Ángulos de Diseño
CREATE TABLE angulos_diseno (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_angulo_id NUMBER NOT NULL REFERENCES tipos_angulo(id),
    diseno_personalizado_id NUMBER NOT NULL REFERENCES disenos_personalizados(id) ON DELETE CASCADE,
    elemento_texto_id NUMBER REFERENCES elementos_texto(id) ON DELETE CASCADE,
    elemento_imagen_id NUMBER REFERENCES elementos_imagen(id) ON DELETE CASCADE,
    thumbnail_resource_id NUMBER REFERENCES cloudinary_resources(id) ON DELETE SET NULL,
    CHECK ((elemento_texto_id IS NULL) OR (elemento_imagen_id IS NULL))
);

-- 15. Producto (catálogo)
CREATE TABLE productos (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    diseno_personalizado_id NUMBER NOT NULL REFERENCES disenos_personalizados(id) ON DELETE CASCADE,
    nombre VARCHAR2(255) NOT NULL,
    descripcion CLOB,
    categoria_id NUMBER NOT NULL REFERENCES categorias(id),
    activo NUMBER(1) DEFAULT 1 NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 16. Carrito
CREATE TABLE carritos (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id NUMBER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 17. Ítems del Carrito
CREATE TABLE items_carrito (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    carrito_id NUMBER NOT NULL REFERENCES carritos(id) ON DELETE CASCADE,
    producto_id NUMBER REFERENCES productos(id) ON DELETE CASCADE,
    diseno_personalizado_id NUMBER REFERENCES disenos_personalizados(id) ON DELETE CASCADE,
    cantidad NUMBER DEFAULT 1 NOT NULL,
    precio_unitario NUMBER(10, 2) NOT NULL,
    color_id VARCHAR2(20) NOT NULL REFERENCES colores(id),
    talla_id VARCHAR2(10) NOT NULL REFERENCES tallas(id),
    tipo_item_id NUMBER NOT NULL REFERENCES tipos_item(id),
    CHECK ((producto_id IS NOT NULL AND diseno_personalizado_id IS NULL) OR 
           (producto_id IS NULL AND diseno_personalizado_id IS NOT NULL))
);

-- 18. Orden
CREATE TABLE ordenes (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id NUMBER REFERENCES usuarios(id) ON DELETE SET NULL,
    estado_id NUMBER NOT NULL REFERENCES estados_orden(id),
    precio_total NUMBER(10, 2) NOT NULL,
    codigo_seguimiento VARCHAR2(50) UNIQUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 19. Información de Envío
CREATE TABLE info_envio (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orden_id NUMBER NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    nombre_completo VARCHAR2(255) NOT NULL,
    direccion CLOB NOT NULL,
    ciudad VARCHAR2(100) NOT NULL,
    estado VARCHAR2(100) NOT NULL,
    codigo_postal VARCHAR2(20) NOT NULL,
    pais VARCHAR2(100) DEFAULT 'Chile' NOT NULL,
    telefono VARCHAR2(20) NOT NULL,
    email VARCHAR2(255) NOT NULL
);

-- 20. Información de Pago
CREATE TABLE info_pago (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orden_id NUMBER NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    metodo_pago_id NUMBER NOT NULL REFERENCES metodos_pago(id),
    ultimos_digitos VARCHAR2(4),
    titular_tarjeta VARCHAR2(255),
    id_transaccion VARCHAR2(255),
    UNIQUE(orden_id)
);

-- 21. Ítems de Orden
CREATE TABLE items_orden (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orden_id NUMBER NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    producto_id NUMBER REFERENCES productos(id) ON DELETE SET NULL,
    diseno_personalizado_id NUMBER REFERENCES disenos_personalizados(id) ON DELETE SET NULL,
    tipo_item_id NUMBER NOT NULL REFERENCES tipos_item(id),
    nombre VARCHAR2(255) NOT NULL,
    color_id VARCHAR2(20) NOT NULL REFERENCES colores(id),
    talla_id VARCHAR2(10) NOT NULL REFERENCES tallas(id),
    cantidad NUMBER NOT NULL,
    precio_unitario NUMBER(10, 2) NOT NULL,
    precio_total NUMBER(10, 2) NOT NULL,
    CHECK ((producto_id IS NOT NULL AND diseno_personalizado_id IS NULL) OR 
           (producto_id IS NULL AND diseno_personalizado_id IS NOT NULL))
);

-- 22. Thumbnails de ítems (UNIFICADA para carrito y orden)
CREATE TABLE thumbnails_item (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_carrito_id NUMBER REFERENCES items_carrito(id) ON DELETE CASCADE,
    item_orden_id NUMBER REFERENCES items_orden(id) ON DELETE CASCADE,
    tipo_angulo_id NUMBER NOT NULL REFERENCES tipos_angulo(id),
    cloudinary_resource_id NUMBER NOT NULL REFERENCES cloudinary_resources(id) ON DELETE CASCADE,
    CHECK ((item_carrito_id IS NOT NULL AND item_orden_id IS NULL) OR 
           (item_carrito_id IS NULL AND item_orden_id IS NOT NULL)),
    UNIQUE(item_carrito_id, tipo_angulo_id),
    UNIQUE(item_orden_id, tipo_angulo_id)
);

-- 23. Historial de Estados de Orden
CREATE TABLE historial_estados_orden (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orden_id NUMBER NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    estado_id NUMBER NOT NULL REFERENCES estados_orden(id),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    nota CLOB
);

-- Inserción de datos para tablas de normalización

-- Colores predefinidos
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('white', 'Blanco', '#FFFFFF', '#000000', 0);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('black', 'Negro', '#1A1A1A', '#FFFFFF', 0);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('gray', 'Gris', '#808080', '#FFFFFF', 0);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('blue', 'Azul', '#0047AB', '#FFFFFF', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('navy', 'Azul Marino', '#000080', '#FFFFFF', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('lightblue', 'Azul Claro', '#ADD8E6', '#000000', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('red', 'Rojo', '#FF0000', '#FFFFFF', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('burgundy', 'Borgoña', '#800020', '#FFFFFF', 700);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('pink', 'Rosa', '#FFC0CB', '#000000', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('green', 'Verde', '#008000', '#FFFFFF', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('olive', 'Verde Oliva', '#808000', '#FFFFFF', 700);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('mint', 'Menta', '#98FF98', '#000000', 700);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('purple', 'Púrpura', '#800080', '#FFFFFF', 700);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('yellow', 'Amarillo', '#FFFF00', '#000000', 500);
INSERT INTO colores (id, nombre, codigo_hex, texto_preview, precio_adicional) VALUES ('orange', 'Naranja', '#FFA500', '#000000', 500);

-- Tallas predefinidas
INSERT INTO tallas (id, nombre, precio_adicional) VALUES ('xs', 'XS', -1000);
INSERT INTO tallas (id, nombre, precio_adicional) VALUES ('s', 'S', -500);
INSERT INTO tallas (id, nombre, precio_adicional) VALUES ('m', 'M', 0);
INSERT INTO tallas (id, nombre, precio_adicional) VALUES ('l', 'L', 500);
INSERT INTO tallas (id, nombre, precio_adicional) VALUES ('xl', 'XL', 1000);
INSERT INTO tallas (id, nombre, precio_adicional) VALUES ('xxl', 'XXL', 1500);

-- Fuentes predefinidas
INSERT INTO fuentes (nombre, url) VALUES ('Arial', NULL);
INSERT INTO fuentes (nombre, url) VALUES ('Helvetica', NULL);
INSERT INTO fuentes (nombre, url) VALUES ('Times New Roman', NULL);
INSERT INTO fuentes (nombre, url) VALUES ('Georgia', NULL);
INSERT INTO fuentes (nombre, url) VALUES ('Verdana', NULL);

-- Estados de diseño predefinidos
INSERT INTO estados_diseno (codigo, nombre, descripcion) VALUES ('PENDING', 'Pendiente', 'El diseño está pendiente de revisión');
INSERT INTO estados_diseno (codigo, nombre, descripcion) VALUES ('APPROVED', 'Aprobado', 'El diseño ha sido aprobado');
INSERT INTO estados_diseno (codigo, nombre, descripcion) VALUES ('REJECTED', 'Rechazado', 'El diseño ha sido rechazado');
INSERT INTO estados_diseno (codigo, nombre, descripcion) VALUES ('MODIFICATION_REQUESTED', 'Modificación Solicitada', 'Se ha solicitado modificar el diseño');

-- Estados de orden predefinidos
INSERT INTO estados_orden (codigo, nombre, descripcion) VALUES ('PENDING', 'Pendiente', 'La orden está pendiente de pago');
INSERT INTO estados_orden (codigo, nombre, descripcion) VALUES ('PAID', 'Pagada', 'La orden ha sido pagada');
INSERT INTO estados_orden (codigo, nombre, descripcion) VALUES ('PROCESSING', 'En Proceso', 'La orden está siendo procesada');
INSERT INTO estados_orden (codigo, nombre, descripcion) VALUES ('SHIPPED', 'Enviada', 'La orden ha sido enviada');
INSERT INTO estados_orden (codigo, nombre, descripcion) VALUES ('DELIVERED', 'Entregada', 'La orden ha sido entregada');
INSERT INTO estados_orden (codigo, nombre, descripcion) VALUES ('CANCELED', 'Cancelada', 'La orden ha sido cancelada');

-- Métodos de pago predefinidos
INSERT INTO metodos_pago (codigo, nombre) VALUES ('CREDIT_CARD', 'Tarjeta de Crédito');
INSERT INTO metodos_pago (codigo, nombre) VALUES ('DEBIT_CARD', 'Tarjeta de Débito');
INSERT INTO metodos_pago (codigo, nombre) VALUES ('TRANSFER', 'Transferencia Bancaria');
INSERT INTO metodos_pago (codigo, nombre) VALUES ('PAYPAL', 'PayPal');

-- Tipos de ángulo predefinidos
INSERT INTO tipos_angulo (codigo, nombre) VALUES ('front', 'Frente');
INSERT INTO tipos_angulo (codigo, nombre) VALUES ('back', 'Espalda');
INSERT INTO tipos_angulo (codigo, nombre) VALUES ('left', 'Izquierda');
INSERT INTO tipos_angulo (codigo, nombre) VALUES ('right', 'Derecha');

-- Tipos de ítem predefinidos
INSERT INTO tipos_item (codigo, nombre) VALUES ('PRODUCT', 'Producto de Catálogo');
INSERT INTO tipos_item (codigo, nombre) VALUES ('CUSTOM', 'Diseño Personalizado');


INSERT INTO usuarios (email, nombre, apellido, oid, telefono, direccion, ciudad, estado, pais, codigo_postal) VALUES ('test@test.cl', 'test', 'test', 'OIDTEST', '999999999', 'Direccion test', 'Ciudad test', 'Estado test', 'Pais test', 'Codigo postal test');

COMMIT;