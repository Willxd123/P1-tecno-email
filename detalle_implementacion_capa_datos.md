# Informe de Implementación — Capa de Datos (Grupo 16)

Este documento detalla la estructura y el mapeo de la **Capa de Datos** (`CapaDatos`) del proyecto **TecnoEmailZUZU**, la cual incluye el módulo monetario, el módulo de insumos y el de **Identidad y Acceso (roles y usuarios)**. Está implementada siguiendo una arquitectura limpia de tres capas (Presentación, Negocio, Datos) y adaptada para ejecutarse bajo **Java 8** con compatibilidad absoluta y alto rendimiento.

---

## 1. Estructura del Código Fuente

Los archivos se han distribuido bajo el directorio [src/CapaDatos](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos) respetando la modularidad y el uso de enums tipados:

```text
src/
└── CapaDatos/
    ├── Conexion.java                       # Administrador de conexiones JDBC
    ├── DRoles.java                         # Clase de Datos para tabla 'roles' (NUEVO)
    ├── DUsuarios.java                      # Clase de Datos para tabla 'usuarios' (UPDATED with rol_id)
    ├── DProductos.java                      # Clase de Datos para tabla 'productos'
    ├── DPedidos.java                        # Clase de Datos para tabla 'pedidos'
    ├── DDetallePedido.java                  # Clase de Datos para tabla 'detalle_pedido'
    ├── DPagos.java                          # Clase de Datos para tabla 'pagos'
    ├── DCuotas.java                         # Clase de Datos para tabla 'cuotas'
    ├── DRecetas.java                        # Clase de Datos para tabla 'recetas'
    ├── DRecetaDetalle.java                  # Clase de Datos para tabla 'receta_detalle'
    ├── DInsumos.java                        # Clase de Datos para tabla 'insumos'
    ├── DMovimientosInsumo.java              # Clase de Datos para tabla 'movimientos_insumo'
    ├── DEnvases.java                        # Clase de Datos para tabla 'envases' (NUEVO)
    ├── DPedidoEnvase.java                  # Clase de Datos para tabla 'pedido_envase' (NUEVO)
    └── enums/                               # Enums nativos para restricciones
        ├── RolNombre.java                   # Propietario, Secretaria, Cliente (NUEVO)
        ├── EstadoPedido.java                # pendiente, pagado, cancelado, entregado
        ├── TipoPago.java                    # contado, cuotas
        ├── UnidadMedida.java                # kg, g, l, ml, unidad
        └── TipoMovimientoInsumo.java        # entrada, consumo, ajuste, merma
```

---

## 2. Mapeo Clase-Tabla y Atributos

Se ha respetado con precisión exacta la nomenclatura de las tablas y atributos definidos en el diseño relacional:

| Clase Java                                                                                                                        | Tabla PostgreSQL     | Atributos Mapeados (Campos Clave)                                                                                                                                |
| :-------------------------------------------------------------------------------------------------------------------------------- | :------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [DRoles.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DRoles.java)                         | `roles`              | `id` (PK), `nombre` (Unique)                                                                                                                                     |
| [DUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DUsuarios.java)                   | `usuarios`           | `id` (PK), `nombre`, `apellido`, `telefono` (Unique), `email` (Unique), `password`, `rol_id` (FK → `roles(id)`)                                     |
| [DProductos.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DProductos.java)                 | `productos`          | `id` (PK), `nombre` (Unique), `descripcion`, `precio_unitario`, `disponible`                                                                                     |
| [DPedidos.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DPedidos.java)                     | `pedidos`            | `id` (PK), `fecha`, `estado` (Enum), `total`, `usuario_id` (FK)                                                                                                  |
| [DDetallePedido.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DDetallePedido.java)         | `detalle_pedido`     | `id` (PK), `cantidad`, `precio_unitario`, `pedido_id` (FK), `producto_id` (FK)                                                                                   |
| [DPagos.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DPagos.java)                         | `pagos`              | `id` (PK), `fecha`, `tipo_pago` (Enum), `pedido_id` (FK, Unique 1:1)                                                                                             |
| [DCuotas.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DCuotas.java)                       | `cuotas`             | `id` (PK), `numero_cuota`, `monto_cuota`, `fecha_vencimiento`, `fecha_pago` (Null), `pagado`, `pago_id` (FK)                                                     |
| [DRecetas.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DRecetas.java)                     | `recetas`            | `id` (PK), `nombre`, `descripcion`, `creado_en`, `producto_id` (FK, Unique 1:1)                                                                                  |
| [DRecetaDetalle.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DRecetaDetalle.java)         | `receta_detalle`     | `id` (PK), `cantidad`, `receta_id` (FK), `insumo_id` (FK, Composite Unique)                                                                                      |
| [DInsumos.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DInsumos.java)                     | `insumos`            | `id` (PK), `nombre` (Unique), `unidad_medida` (Enum), `stock_actual`, `stock_minimo`, `costo_unitario`                                                           |
| [DMovimientosInsumo.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DMovimientosInsumo.java) | `movimientos_insumo` | `id` (PK), `fecha`, `tipo` (Enum), `cantidad`, `descripcion`, `insumo_id` (FK), `pedido_id` (FK, Nullable)                                                       |
| [DEnvases.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DEnvases.java)                     | `envases`            | `id` (PK), `nombre` (Unique), `descripcion`, `stock_total`, `stock_disponible`                                                                                   |
| [DPedidoEnvase.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/DPedidoEnvase.java)           | `pedido_envase`      | `id` (PK), `cantidad_prestada`, `cantidad_devuelta`, `fecha_devolucion` (Null), `pedido_origen_id` (FK), `pedido_devolucion_id` (FK, Nullable), `envase_id` (FK) |

---

## 3. Manejo de Enums e Integridad

Para mantener la seguridad y las restricciones indicadas por el docente y los casos de uso, cada columna que opera con conjuntos cerrados de valores cuenta con un **Java Enum** ubicado en [src/CapaDatos/enums](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaDatos/enums):

1. **`RolNombre`**: `Propietario`, `Secretaria`, `Cliente`.
2. **`EstadoPedido`**: `pendiente`, `pagado`, `cancelado`, `entregado`.
3. **`TipoPago`**: `contado`, `cuotas`.
4. **`UnidadMedida`**: `kg`, `g`, `l`, `ml`, `unidad`.
5. **`TipoMovimientoInsumo`**: `entrada`, `consumo`, `ajuste`, `merma`.

---

## 4. Biblioteca JDBC y Configuración de Conectores

Para que el proyecto trabaje de forma directa:

- Se ha configurado la carpeta `lib/` y descargado el conector oficial de PostgreSQL: **[postgresql-42.7.3.jar](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/lib/postgresql-42.7.3.jar)**.
- Se ha configurado el archivo `.vscode/settings.json` añadiendo la propiedad `"java.project.referencedLibraries": ["lib/**/*.jar"]` a fin de que Antigravity indexe las librerías automáticamente para tu comodidad.

---

## 5. Script DDL de Creación de Base de Datos Actualizado (PostgreSQL)

Aquí tienes el script SQL completo y verificado para crear la estructura relacional que incluye la tabla `roles` y la clave foránea en la tabla `usuarios`, junto con la precarga de los roles del **Caso de Uso 1**:

```sql
-- ==========================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS - GRUPO 16 (POSTGRESQL)
-- ==========================================================

-- MÓDULO 0: ROLES Y ACCESOS

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Precargar roles requeridos para el Caso de Uso 1 (CU1)
INSERT INTO roles (nombre) VALUES ('Propietario'), ('Secretaria'), ('Cliente');

-- MÓDULO 1: IDENTIDAD Y CATÁLOGO

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    telefono VARCHAR(20) UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id INT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT
);

CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion TEXT,
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    disponible BOOLEAN DEFAULT TRUE
);

-- MÓDULO 2: TRANSACCIONES Y PAGOS

CREATE TABLE pedidos (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW(),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('pendiente', 'pagado', 'cancelado', 'entregado')),
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE detalle_pedido (
    id SERIAL PRIMARY KEY,
    cantidad SMALLINT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    pedido_id INT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT
);

CREATE TABLE pagos (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW(),
    tipo_pago VARCHAR(15) NOT NULL CHECK (tipo_pago IN ('contado', 'cuotas')),
    pedido_id INT NOT NULL UNIQUE REFERENCES pedidos(id) ON DELETE CASCADE
);

CREATE TABLE cuotas (
    id SERIAL PRIMARY KEY,
    numero_cuota SMALLINT NOT NULL CHECK (numero_cuota > 0),
    monto_cuota DECIMAL(10,2) NOT NULL CHECK (monto_cuota > 0),
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE NULL,
    pagado BOOLEAN DEFAULT FALSE,
    pago_id INT NOT NULL REFERENCES pagos(id) ON DELETE CASCADE
);

-- MÓDULO 3: INSUMOS Y RECETAS

CREATE TABLE insumos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    unidad_medida VARCHAR(20) NOT NULL CHECK (unidad_medida IN ('kg', 'g', 'l', 'ml', 'unidad')),
    stock_actual DECIMAL(10,3) NOT NULL CHECK (stock_actual >= 0),
    stock_minimo DECIMAL(10,3) NOT NULL CHECK (stock_minimo >= 0),
    costo_unitario DECIMAL(10,2) NOT NULL CHECK (costo_unitario > 0)
);

CREATE TABLE recetas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    creado_en TIMESTAMP DEFAULT NOW(),
    producto_id INT NOT NULL UNIQUE REFERENCES productos(id) ON DELETE CASCADE
);

CREATE TABLE receta_detalle (
    id SERIAL PRIMARY KEY,
    cantidad DECIMAL(10,3) NOT NULL CHECK (cantidad > 0),
    receta_id INT NOT NULL REFERENCES recetas(id) ON DELETE CASCADE,
    insumo_id INT NOT NULL REFERENCES insumos(id) ON DELETE RESTRICT,
    UNIQUE (receta_id, insumo_id)
);

CREATE TABLE movimientos_insumo (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT NOW(),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('entrada', 'consumo', 'ajuste', 'merma')),
    cantidad DECIMAL(10,3) NOT NULL CHECK (cantidad <> 0),
    descripcion TEXT,
    insumo_id INT NOT NULL REFERENCES insumos(id) ON DELETE CASCADE,
    pedido_id INT NULL REFERENCES pedidos(id) ON DELETE SET NULL
);

-- MÓDULO 4: GESTIÓN DE ENVASES

CREATE TABLE envases (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion TEXT,
    stock_total INT NOT NULL CHECK (stock_total > 0),
    stock_disponible INT NOT NULL CHECK (stock_disponible >= 0),
    CONSTRAINT chk_stock_disponible CHECK (stock_disponible <= stock_total)
);

CREATE TABLE pedido_envase (
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
```

---

## 6. Comandos de Compilación y Verificación

Para compilar y verificar este proyecto entero utilizando el conector descargado, posicionarse en la carpeta [TecnoEmailZUZU/](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU) y ejecutar en tu terminal de Windows:

```powershell
# Compilar todo el proyecto hacia la carpeta /bin vinculando el driver de PostgreSQL
javac --release 8 -d bin -cp "lib/postgresql-42.7.3.jar" -sourcepath src src/Main.java src/CapaDatos/*.java src/CapaDatos/enums/*.java src/configuracion/*.java

# Ejecutar el punto de entrada
java -cp "bin;lib/postgresql-42.7.3.jar" Main
```
