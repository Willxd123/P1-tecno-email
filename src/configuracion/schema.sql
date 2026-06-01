-- ==========================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS - GRUPO 16 (POSTGRESQL)
-- ==========================================================

-- MÓDULO 0: ROLES Y ACCESOS
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Precargar roles requeridos para el Caso de Uso 1 (CU1) sin duplicar
INSERT INTO roles (nombre) VALUES 
('Propietario'), 
('Secretaria'), 
('Cliente')
ON CONFLICT (nombre) DO NOTHING;

-- MÓDULO 1: IDENTIDAD Y CATÁLOGO
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    telefono VARCHAR(20) UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id INT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion TEXT,
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    disponible BOOLEAN DEFAULT TRUE
);

-- MÓDULO 2: TRANSACCIONES Y PAGOS
CREATE TABLE IF NOT EXISTS pedidos (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW(),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('pendiente', 'pagado', 'cancelado', 'entregado')),
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id SERIAL PRIMARY KEY,
    cantidad SMALLINT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    pedido_id INT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS pagos (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW(),
    tipo_pago VARCHAR(15) NOT NULL CHECK (tipo_pago IN ('contado', 'cuotas')),
    pedido_id INT NOT NULL UNIQUE REFERENCES pedidos(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cuotas (
    id SERIAL PRIMARY KEY,
    numero_cuota SMALLINT NOT NULL CHECK (numero_cuota > 0),
    monto_cuota DECIMAL(10,2) NOT NULL CHECK (monto_cuota > 0),
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE NULL,
    pagado BOOLEAN DEFAULT FALSE,
    pago_id INT NOT NULL REFERENCES pagos(id) ON DELETE CASCADE
);

-- MÓDULO 3: INSUMOS Y RECETAS
CREATE TABLE IF NOT EXISTS insumos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    unidad_medida VARCHAR(20) NOT NULL CHECK (unidad_medida IN ('kg', 'g', 'l', 'ml', 'unidad')),
    stock_actual DECIMAL(10,3) NOT NULL CHECK (stock_actual >= 0),
    stock_minimo DECIMAL(10,3) NOT NULL CHECK (stock_minimo >= 0),
    costo_unitario DECIMAL(10,2) NOT NULL CHECK (costo_unitario > 0)
);

CREATE TABLE IF NOT EXISTS recetas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    producto_id INT NOT NULL UNIQUE REFERENCES productos(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS receta_detalle (
    id SERIAL PRIMARY KEY,
    cantidad DECIMAL(10,3) NOT NULL CHECK (cantidad > 0),
    receta_id INT NOT NULL REFERENCES recetas(id) ON DELETE CASCADE,
    insumo_id INT NOT NULL REFERENCES insumos(id) ON DELETE RESTRICT,
    UNIQUE (receta_id, insumo_id)
);

CREATE TABLE IF NOT EXISTS movimientos_insumo (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW(),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('entrada', 'consumo', 'ajuste', 'merma')),
    cantidad DECIMAL(10,3) NOT NULL CHECK (cantidad <> 0),
    descripcion TEXT,
    insumo_id INT NOT NULL REFERENCES insumos(id) ON DELETE CASCADE,
    pedido_id INT NULL REFERENCES pedidos(id) ON DELETE SET NULL
);

-- MÓDULO 4: GESTIÓN DE ENVASES
CREATE TABLE IF NOT EXISTS envases (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion TEXT,
    stock_total INT NOT NULL CHECK (stock_total > 0),
    stock_disponible INT NOT NULL CHECK (stock_disponible >= 0),
    CONSTRAINT chk_stock_disponible CHECK (stock_disponible <= stock_total)
);

CREATE TABLE IF NOT EXISTS pedido_envase (
    id SERIAL PRIMARY KEY,
    cantidad_prestada INT NOT NULL CHECK (cantidad_prestada > 0),
    cantidad_devuelta INT NOT NULL DEFAULT 0 CHECK (cantidad_devuelta >= 0),
    fecha_devolucion TIMESTAMP,
    pedido_origen_id INT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    pedido_devolucion_id INT REFERENCES pedidos(id) ON DELETE SET NULL,
    envase_id INT NOT NULL REFERENCES envases(id) ON DELETE RESTRICT,
    CONSTRAINT chk_cantidad_devuelta CHECK (cantidad_devuelta <= cantidad_prestada),
    UNIQUE (pedido_origen_id, envase_id)
);
