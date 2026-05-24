Perfecto, el modelo queda muy claro con esas respuestas. Antes de darte las tablas, te explico la lógica que seguí:

**Envases** son entidades independientes del producto. Se prestan al momento de entregar un pedido y se devuelven al momento de hacer el siguiente. El sistema debe saber en todo momento cuántos envases tiene prestados cada usuario.

**La cartilla** no es una tabla separada — es una vista del historial por usuario. Se construye consultando `pedidos` + `detalle_pedido` + `envase_prestamo`. No necesita tabla propia porque toda la información ya existe en el sistema. Lo que sí necesitas es la tabla de préstamos de envases.

**La devolución al siguiente pedido** significa que cuando Carlos hace su pedido 3, en ese momento se registra que devolvió los envases del pedido 2. Eso se modela con una referencia cruzada entre pedidos.

---

## Tablas nuevas

---

### `envases`

Catálogo maestro de tipos de envase que maneja la repostería. Genéricos, no atados a ningún producto.

| Atributo           | Tipo de dato   | Función                                                                                         | Enum / restricción |
| ------------------ | -------------- | ----------------------------------------------------------------------------------------------- | ------------------ |
| `id` PK            | `SERIAL`       | Identificador único del tipo de envase                                                          | —                  |
| `nombre`           | `VARCHAR(120)` | Nombre descriptivo (ej. "Caja mediana", "Recipiente redondo")                                   | NOT NULL, UNIQUE   |
| `descripcion`      | `TEXT`         | Detalle adicional del envase                                                                    | —                  |
| `stock_total`      | `INT`          | Cantidad total de envases de este tipo que posee la empresa                                     | NOT NULL, > 0      |
| `stock_disponible` | `INT`          | Cantidad actualmente en la repostería, sin prestar. Se actualiza con cada préstamo y devolución | NOT NULL, >= 0     |

> `stock_disponible` nunca puede superar `stock_total`. Se puede verificar con un CHECK constraint: `stock_disponible <= stock_total`.

---

### `pedido_envase`

Registra cuántos envases de cada tipo se prestaron en un pedido específico y si ya fueron devueltos. Es la tabla central de la gestión de envases.

| Atributo                  | Tipo de dato | Función                                                                         | Enum / restricción        |
| ------------------------- | ------------ | ------------------------------------------------------------------------------- | ------------------------- |
| `id` PK                   | `SERIAL`     | Identificador único del préstamo                                                | —                         |
| `cantidad_prestada`       | `INT`        | Cantidad de envases de este tipo entregados al cliente en este pedido           | NOT NULL, > 0             |
| `cantidad_devuelta`       | `INT`        | Cantidad efectivamente devuelta. Empieza en 0, se actualiza al siguiente pedido | NOT NULL, >= 0, DEFAULT 0 |
| `fecha_devolucion`        | `TIMESTAMP`  | Momento en que se registró la devolución. NULL si aún no devolvió               | —                         |
| `pedido_origen_id` FK     | `INT`        | Pedido en que se prestaron los envases                                          | NOT NULL → `pedidos(id)`  |
| `pedido_devolucion_id` FK | `INT`        | Pedido en que se devolvieron los envases. NULL hasta que ocurra la devolución   | → `pedidos(id)`           |
| `envase_id` FK            | `INT`        | Tipo de envase prestado                                                         | NOT NULL → `envases(id)`  |

> `UNIQUE` en `(pedido_origen_id, envase_id)` para no duplicar el mismo tipo de envase en el mismo pedido.

> `cantidad_devuelta <= cantidad_prestada` verificable con CHECK constraint.

---

## La cartilla es una consulta, no una tabla

La cartilla de un usuario es el resultado de esta consulta. No hay que mantener una tabla adicional porque todo está en el sistema:

```sql
SELECT
    p.id                        AS pedido_id,
    p.fecha                     AS fecha_pedido,
    p.total                     AS monto_pedido,
    p.estado                    AS estado_pedido,
    e.nombre                    AS tipo_envase,
    pe.cantidad_prestada,
    pe.cantidad_devuelta,
    pe.fecha_devolucion,
    CASE
        WHEN pe.cantidad_devuelta = pe.cantidad_prestada THEN 'devuelto'
        WHEN pe.cantidad_devuelta > 0 THEN 'devuelto parcial'
        WHEN pe.pedido_origen_id IS NULL THEN 'sin envase'
        ELSE 'pendiente'
    END                         AS estado_envase
FROM pedidos p
LEFT JOIN pedido_envase pe ON pe.pedido_origen_id = p.id
LEFT JOIN envases e        ON e.id = pe.envase_id
WHERE p.usuario_id = :usuario_id
ORDER BY p.fecha DESC;
```

---

## Datos de ejemplo — flujo completo con envases

---

### `envases`

| id  | nombre       | descripcion                             | stock_total | stock_disponible |
| --- | ------------ | --------------------------------------- | ----------- | ---------------- |
| 1   | Caja mediana | Caja de cartón para tortas individuales | 20          | 20               |

---

**Carlos hace su primer pedido (pedido #2, 1 torta). Se le presta 1 caja.**

### `pedido_envase` — al entregar pedido #2

| id  | pedido_origen_id | pedido_devolucion_id | envase_id | cantidad_prestada | cantidad_devuelta | fecha_devolucion |
| --- | ---------------- | -------------------- | --------- | ----------------- | ----------------- | ---------------- |
| 1   | 2                | NULL                 | 1         | 1                 | 0                 | NULL             |

### `envases` — stock tras prestar al pedido #2

| id  | nombre       | stock_total | stock_disponible |
| --- | ------------ | ----------- | ---------------- |
| 1   | Caja mediana | 20          | 19               |

---

**Carlos hace su segundo pedido (pedido #3). En ese momento devuelve la caja del pedido #2.**

### `pedido_envase` — al registrar devolución en pedido #3

| id  | pedido_origen_id | pedido_devolucion_id | envase_id | cantidad_prestada | cantidad_devuelta | fecha_devolucion    |
| --- | ---------------- | -------------------- | --------- | ----------------- | ----------------- | ------------------- |
| 1   | 2                | 3                    | 1         | 1                 | 1                 | 2026-05-11 09:00:00 |

### `envases` — stock tras devolución

| id  | nombre       | stock_total | stock_disponible |
| --- | ------------ | ----------- | ---------------- |
| 1   | Caja mediana | 20          | 20               |

---

**Cartilla de Carlos — resultado de la consulta**

| pedido_id | fecha_pedido        | monto_pedido | estado_pedido | tipo_envase  | cant_prestada | cant_devuelta | fecha_devolucion    | estado_envase |
| --------- | ------------------- | ------------ | ------------- | ------------ | ------------- | ------------- | ------------------- | ------------- |
| 3         | 2026-05-11 09:00:00 | 100.00       | pagado        | Caja mediana | 1             | 1             | 2026-05-11 09:00:00 | devuelto      |
| 2         | 2026-05-04 14:00:00 | 100.00       | pendiente     | Caja mediana | 1             | 1             | 2026-05-11 09:00:00 | devuelto      |

---

## Resumen de relaciones nuevas

| Relación                                 | Tipo | Detalle                                                      |
| ---------------------------------------- | ---- | ------------------------------------------------------------ |
| `pedidos` → `pedido_envase` (origen)     | 1:N  | Un pedido puede prestar varios tipos de envase               |
| `pedidos` → `pedido_envase` (devolución) | 1:N  | Un pedido puede registrar devoluciones de pedidos anteriores |
| `envases` → `pedido_envase`              | 1:N  | Un tipo de envase puede aparecer en muchos préstamos         |

---

## Vista del administrador — envases pendientes

Esta consulta le muestra al administrador quién tiene envases sin devolver:

```sql
SELECT
    u.nombre,
    u.apellido,
    u.telefono,
    e.nombre                AS tipo_envase,
    SUM(pe.cantidad_prestada - pe.cantidad_devuelta) AS envases_pendientes,
    MIN(p.fecha)            AS desde_fecha
FROM pedido_envase pe
JOIN pedidos p   ON p.id = pe.pedido_origen_id
JOIN usuarios u  ON u.id = p.usuario_id
JOIN envases e   ON e.id = pe.envase_id
WHERE pe.cantidad_devuelta < pe.cantidad_prestada
GROUP BY u.id, u.nombre, u.apellido, u.telefono, e.nombre
ORDER BY envases_pendientes DESC;
```

## Mapeo de tablas — módulo monetario

---

### `roles` — Roles de usuario

Almacena los diferentes roles que pueden asignarse a los usuarios del sistema.

| Atributo | Tipo de dato   | Función                                            | Enum / restricción |
| -------- | -------------- | -------------------------------------------------- | ------------------ |
| `id` PK  | `SERIAL`       | Identificador único del rol                        | —                  |
| `nombre` | `VARCHAR(50)`  | Nombre único del rol (Propietario, Secretaria...)  | NOT NULL, UNIQUE   |

---

### `usuarios` — Identidad y acceso

Almacena los datos personales y credenciales de quienes realizan pedidos.

| Atributo    | Tipo de dato   | Función                                                | Enum / restricción           |
| ----------- | -------------- | ------------------------------------------------------ | ---------------------------- |
| `id` PK     | `SERIAL`       | Identificador único autoincremental                    | —                            |
| `nombre`    | `VARCHAR(80)`  | Nombre de pila del cliente                             | NOT NULL                     |
| `apellido`  | `VARCHAR(80)`  | Apellido del cliente                                   | NOT NULL                     |
| `telefono`  | `VARCHAR(20)`  | Contacto telefónico del cliente                        | UNIQUE                       |
| `email`     | `VARCHAR(120)` | Correo electrónico, usado para login y consultas       | NOT NULL, UNIQUE             |
| `password`  | `VARCHAR(255)` | Hash bcrypt/argon2 de la contraseña. Nunca texto plano | NOT NULL                     |
| `rol_id` FK | `INT`          | Rol asignado al usuario                                | NOT NULL → `roles(id)`       |

---

### `productos` — Catálogo

Catálogo maestro de productos de la repostería. El precio aquí es el vigente; el histórico vive en `detalle_pedido`.

| Atributo          | Tipo de dato    | Función                                    | Enum / restricción |
| ----------------- | --------------- | ------------------------------------------ | ------------------ |
| `id` PK           | `SERIAL`        | Identificador único del producto           | —                  |
| `nombre`          | `VARCHAR(120)`  | Nombre comercial del producto              | NOT NULL, UNIQUE   |
| `descripcion`     | `TEXT`          | Descripción larga para mostrar al cliente  | —                  |
| `precio_unitario` | `DECIMAL(10,2)` | Precio de venta vigente                    | NOT NULL, > 0      |
| `disponible`      | `BOOLEAN`       | Permite ocultar el producto sin eliminarlo | DEFAULT TRUE       |

---

### `pedidos` — Transacción principal

Cabecera de cada compra. Es el eje central del sistema.

| Atributo        | Tipo de dato    | Función                                                            | Enum / restricción                                    |
| --------------- | --------------- | ------------------------------------------------------------------ | ----------------------------------------------------- |
| `id` PK         | `SERIAL`        | Identificador único del pedido                                     | —                                                     |
| `fecha`         | `TIMESTAMP`     | Momento exacto en que se creó el pedido                            | DEFAULT NOW()                                         |
| `estado`        | `VARCHAR(20)`   | Estado actual del pedido en el flujo de negocio                    | ENUM: `pendiente`, `pagado`, `cancelado`, `entregado` |
| `total`         | `DECIMAL(10,2)` | Suma calculada al confirmar el pedido y persistida por rendimiento | NOT NULL, >= 0                                        |
| `usuario_id` FK | `INT`           | Usuario que realizó el pedido                                      | NOT NULL → `usuarios(id)`                             |

---

### `detalle_pedido` — Líneas de compra

Desglosa cada pedido en sus productos. Guarda el precio al momento de la venta para preservar historial.

| Atributo          | Tipo de dato    | Función                                                    | Enum / restricción         |
| ----------------- | --------------- | ---------------------------------------------------------- | -------------------------- |
| `id` PK           | `SERIAL`        | Identificador único de la línea                            | —                          |
| `cantidad`        | `SMALLINT`      | Unidades del producto solicitadas                          | NOT NULL, > 0              |
| `precio_unitario` | `DECIMAL(10,2)` | Precio en el momento de la venta, desacoplado del catálogo | NOT NULL, > 0              |
| `pedido_id` FK    | `INT`           | Pedido al que pertenece esta línea                         | NOT NULL → `pedidos(id)`   |
| `producto_id` FK  | `INT`           | Producto referenciado en esta línea                        | NOT NULL → `productos(id)` |

---

### `pagos` — Registro de pago

Registra cómo se paga cada pedido. Relación 1:1 con pedidos. No guarda total porque es derivable.

| Atributo       | Tipo de dato  | Función                                                                 | Enum / restricción               |
| -------------- | ------------- | ----------------------------------------------------------------------- | -------------------------------- |
| `id` PK        | `SERIAL`      | Identificador único del pago                                            | —                                |
| `fecha`        | `TIMESTAMP`   | Momento en que se registró el pago                                      | DEFAULT NOW()                    |
| `tipo_pago`    | `VARCHAR(15)` | Define si el pago es inmediato o diferido. Determina si se crean cuotas | ENUM: `contado`, `cuotas`        |
| `pedido_id` FK | `INT`         | Pedido al que corresponde. Un pedido solo tiene un pago                 | NOT NULL, UNIQUE → `pedidos(id)` |

---

### `cuotas` — Plan de pagos

Solo existe cuando `tipo_pago = 'cuotas'`. Registra vencimiento y pago real para detectar mora.

| Atributo            | Tipo de dato    | Función                                            | Enum / restricción     |
| ------------------- | --------------- | -------------------------------------------------- | ---------------------- |
| `id` PK             | `SERIAL`        | Identificador único de la cuota                    | —                      |
| `numero_cuota`      | `SMALLINT`      | Número de orden dentro del plan (1, 2, 3…)         | NOT NULL, > 0          |
| `monto_cuota`       | `DECIMAL(10,2)` | Importe acordado para esta cuota específica        | NOT NULL, > 0          |
| `fecha_vencimiento` | `DATE`          | Fecha límite pactada, usada para calcular mora     | NOT NULL               |
| `fecha_pago`        | `DATE`          | Fecha real en que se pagó. NULL si aún no se pagó  | —                      |
| `pagado`            | `BOOLEAN`       | Bandera rápida de estado para consultas eficientes | DEFAULT FALSE          |
| `pago_id` FK        | `INT`           | Plan de pago al que pertenece esta cuota           | NOT NULL → `pagos(id)` |

---

## Mapeo de tablas — módulo de insumos

Antes del mapeo, el diseño que propongo para este módulo:

`insumos` es el maestro de materias primas (harina, azúcar, huevos, etc.) con su stock actual y punto de reorden. `recetas` vincula cada producto con sus insumos necesarios, especificando la cantidad requerida por unidad producida — esa es la tabla central del módulo. `movimientos_insumo` registra cada entrada y salida de stock, que es lo que permite tener el inventario en tiempo real sin depender de un campo `stock` que se puede desincronizar.

El modelo queda así:

---

### `recetas` — Cabecera de receta por producto

| Atributo         | Tipo de dato   | Función                                              | Enum / restricción                 |
| ---------------- | -------------- | ---------------------------------------------------- | ---------------------------------- |
| `id` PK          | `SERIAL`       | Identificador único de la receta                     | —                                  |
| `nombre`         | `VARCHAR(120)` | Nombre descriptivo (ej. "Receta torta de chocolate") | NOT NULL                           |
| `descripcion`    | `TEXT`         | Notas del proceso, indicaciones del repostero        | —                                  |
| `creado_en`      | `TIMESTAMP`    | Fecha de registro de la receta                       | DEFAULT NOW()                      |
| `producto_id` FK | `INT`          | Producto al que pertenece esta receta. Relación 1:1  | NOT NULL, UNIQUE → `productos(id)` |

> UNIQUE en `producto_id` garantiza que un producto tenga como máximo una receta.

---

### `receta_detalle` — Insumos de la receta

Cada fila es un ingrediente de la receta. Las cantidades representan exactamente lo necesario para producir una unidad del producto.

| Atributo       | Tipo de dato    | Función                                                          | Enum / restricción       |
| -------------- | --------------- | ---------------------------------------------------------------- | ------------------------ |
| `id` PK        | `SERIAL`        | Identificador único de la línea                                  | —                        |
| `cantidad`     | `DECIMAL(10,3)` | Cantidad exacta del insumo para producir una unidad del producto | NOT NULL, > 0            |
| `receta_id` FK | `INT`           | Receta a la que pertenece esta línea                             | NOT NULL → `recetas(id)` |
| `insumo_id` FK | `INT`           | Insumo requerido en esta línea                                   | NOT NULL → `insumos(id)` |

> UNIQUE en `(receta_id, insumo_id)` para que el mismo insumo no aparezca dos veces en la misma receta.

---

### `insumos` — Maestro de materias primas

| Atributo         | Tipo de dato    | Función                                                               | Enum / restricción                   |
| ---------------- | --------------- | --------------------------------------------------------------------- | ------------------------------------ |
| `id` PK          | `SERIAL`        | Identificador único del insumo                                        | —                                    |
| `nombre`         | `VARCHAR(120)`  | Nombre del insumo (ej. "Harina de trigo")                             | NOT NULL, UNIQUE                     |
| `unidad_medida`  | `VARCHAR(20)`   | Unidad en que se mide y descuenta el stock                            | ENUM: `kg`, `g`, `l`, `ml`, `unidad` |
| `stock_actual`   | `DECIMAL(10,3)` | Stock disponible actualizado con cada movimiento                      | NOT NULL, >= 0                       |
| `stock_minimo`   | `DECIMAL(10,3)` | Umbral de alerta — cuando stock_actual cae por debajo se debe reponer | NOT NULL, >= 0                       |
| `costo_unitario` | `DECIMAL(10,2)` | Costo por unidad de medida, para calcular costo de producción         | NOT NULL, > 0                        |

---

### `movimientos_insumo` — Stock en tiempo real

| Atributo       | Tipo de dato    | Función                                                              | Enum / restricción                            |
| -------------- | --------------- | -------------------------------------------------------------------- | --------------------------------------------- |
| `id` PK        | `SERIAL`        | Identificador único del movimiento                                   | —                                             |
| `fecha`        | `TIMESTAMP`     | Momento exacto del movimiento                                        | DEFAULT NOW()                                 |
| `tipo`         | `VARCHAR(20)`   | Naturaleza del movimiento                                            | ENUM: `entrada`, `consumo`, `ajuste`, `merma` |
| `cantidad`     | `DECIMAL(10,3)` | Positivo para entradas, negativo para consumos y mermas              | NOT NULL, ≠ 0                                 |
| `descripcion`  | `TEXT`          | Observación opcional                                                 | —                                             |
| `insumo_id` FK | `INT`           | Insumo afectado                                                      | NOT NULL → `insumos(id)`                      |
| `pedido_id` FK | `INT`           | Pedido que originó el consumo. NULL para entradas y ajustes manuales | → `pedidos(id)`                               |

---

El flujo al confirmar un pedido con N unidades del producto P queda muy limpio:

1. `productos` → obtener `receta_id` (o consultar `recetas` por `producto_id`).
2. `receta_detalle` → listar todos los insumos y sus cantidades exactas por unidad.
3. Consumo total de cada insumo = `receta_detalle.cantidad × N`.
4. Insertar en `movimientos_insumo` un registro por insumo con cantidad negativa y referencia al pedido.
5. Actualizar `insumos.stock_actual`.
6. Si `stock_actual < stock_minimo` → alerta de reposición.
