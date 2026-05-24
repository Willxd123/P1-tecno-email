## Datos de ejemplo — flujo completo repostería

---

### `usuarios`

| id  | nombre | apellido | telefono | password        |
| --- | ------ | -------- | -------- | --------------- |
| 1   | María  | Flores   | 77712345 | `$2b$10$abc...` |
| 2   | Carlos | Mamani   | 76698765 | `$2b$10$def...` |

---

### `insumos`

| id  | nombre          | descripcion                     | unidad_medida | stock_actual | stock_minimo | costo_unitario |
| --- | --------------- | ------------------------------- | ------------- | ------------ | ------------ | -------------- |
| 1   | Harina de trigo | Harina refinada para repostería | g             | 5000         | 500          | 0.008          |
| 2   | Azúcar          | Azúcar blanca granulada         | g             | 3000         | 300          | 0.005          |
| 3   | Huevos          | Huevos frescos tamaño M         | unidad        | 24           | 6            | 1.50           |
| 4   | Leche           | Leche entera pasteurizada       | ml            | 2000         | 200          | 0.007          |
| 5   | Mantequilla     | Mantequilla sin sal             | g             | 1000         | 100          | 0.015          |
| 6   | Cacao en polvo  | Cacao puro sin azúcar           | g             | 500          | 50           | 0.025          |

---

### `productos`

| id  | nombre             | descripcion                                | precio_unitario | disponible |
| --- | ------------------ | ------------------------------------------ | --------------- | ---------- |
| 1   | Torta de chocolate | Torta esponjosa de chocolate con cobertura | 100.00          | true       |

---

### `recetas`

| id  | nombre                    | descripcion                                         | creado_en           | producto_id |
| --- | ------------------------- | --------------------------------------------------- | ------------------- | ----------- |
| 1   | Receta torta de chocolate | Ingredientes para una torta individual de chocolate | 2026-04-01 07:00:00 | 1           |

---

### `receta_detalle`

| id  | receta_id | insumo_id | cantidad |
| --- | --------- | --------- | -------- |
| 1   | 1         | 1         | 200.000  |
| 2   | 1         | 2         | 150.000  |
| 3   | 1         | 3         | 2.000    |
| 4   | 1         | 4         | 100.000  |
| 5   | 1         | 5         | 80.000   |
| 6   | 1         | 6         | 50.000   |

> Lectura: para producir 1 torta de chocolate se necesitan 200g harina, 150g azúcar, 2 huevos, 100ml leche, 80g mantequilla, 50g cacao.

---

## Caso 1 — María pide 2 tortas, paga al contado

---

### `pedidos`

| id  | fecha               | estado | total  | usuario_id |
| --- | ------------------- | ------ | ------ | ---------- |
| 1   | 2026-05-04 10:00:00 | pagado | 200.00 | 1          |

---

### `detalle_pedido`

| id  | pedido_id | producto_id | cantidad | precio_unitario |
| --- | --------- | ----------- | -------- | --------------- |
| 1   | 1         | 1           | 2        | 100.00          |

> `total pedido` = 2 × 100.00 = **200.00 Bs** ✓

---

### `pagos`

| id  | fecha               | tipo_pago | pedido_id |
| --- | ------------------- | --------- | --------- |
| 1   | 2026-05-04 10:05:00 | contado   | 1         |

---

### `cuotas`

| id  | pago_id | numero_cuota | monto_cuota | fecha_vencimiento | fecha_pago | pagado |
| --- | ------- | ------------ | ----------- | ----------------- | ---------- | ------ |

> Sin registros. `tipo_pago = contado` no genera cuotas.

---

### `movimientos_insumo` — consumo del pedido 1

Cálculo: `receta_detalle.cantidad × 2 unidades`

| id  | insumo_id | tipo    | cantidad | descripcion                               | pedido_id | fecha               |
| --- | --------- | ------- | -------- | ----------------------------------------- | --------- | ------------------- |
| 1   | 1         | consumo | -400.000 | Consumo pedido #1 — 2 tortas de chocolate | 1         | 2026-05-04 10:05:00 |
| 2   | 2         | consumo | -300.000 | Consumo pedido #1 — 2 tortas de chocolate | 1         | 2026-05-04 10:05:00 |
| 3   | 3         | consumo | -4.000   | Consumo pedido #1 — 2 tortas de chocolate | 1         | 2026-05-04 10:05:00 |
| 4   | 4         | consumo | -200.000 | Consumo pedido #1 — 2 tortas de chocolate | 1         | 2026-05-04 10:05:00 |
| 5   | 5         | consumo | -160.000 | Consumo pedido #1 — 2 tortas de chocolate | 1         | 2026-05-04 10:05:00 |
| 6   | 6         | consumo | -100.000 | Consumo pedido #1 — 2 tortas de chocolate | 1         | 2026-05-04 10:05:00 |

---

### `insumos` — stock tras pedido 1

| id  | nombre          | stock_antes | consumo  | stock_actual | stock_minimo | alerta |
| --- | --------------- | ----------- | -------- | ------------ | ------------ | ------ |
| 1   | Harina de trigo | 5000.000    | -400.000 | 4600.000     | 500          | —      |
| 2   | Azúcar          | 3000.000    | -300.000 | 2700.000     | 300          | —      |
| 3   | Huevos          | 24.000      | -4.000   | 20.000       | 6            | —      |
| 4   | Leche           | 2000.000    | -200.000 | 1800.000     | 200          | —      |
| 5   | Mantequilla     | 1000.000    | -160.000 | 840.000      | 100          | —      |
| 6   | Cacao en polvo  | 500.000     | -100.000 | 400.000      | 50           | —      |

---

## Caso 2 — Carlos pide 1 torta, paga en 2 cuotas de 50 Bs

---

### `pedidos`

| id  | fecha               | estado    | total  | usuario_id |
| --- | ------------------- | --------- | ------ | ---------- |
| 2   | 2026-05-04 14:00:00 | pendiente | 100.00 | 2          |

> Estado `pendiente` porque aún no se completaron todas las cuotas.

---

### `detalle_pedido`

| id  | pedido_id | producto_id | cantidad | precio_unitario |
| --- | --------- | ----------- | -------- | --------------- |
| 2   | 2         | 1           | 1        | 100.00          |

> `total pedido` = 1 × 100.00 = **100.00 Bs** ✓

---

### `pagos`

| id  | fecha               | tipo_pago | pedido_id |
| --- | ------------------- | --------- | --------- |
| 2   | 2026-05-04 14:05:00 | cuotas    | 2         |

---

### `cuotas`

| id  | pago_id | numero_cuota | monto_cuota | fecha_vencimiento | fecha_pago | pagado |
| --- | ------- | ------------ | ----------- | ----------------- | ---------- | ------ |
| 1   | 2       | 1            | 50.00       | 2026-05-11        | 2026-05-10 | true   |
| 2   | 2       | 2            | 50.00       | 2026-05-18        | NULL       | false  |

> Cuota 1 pagada antes del vencimiento. Cuota 2 aún pendiente → pedido permanece en `pendiente` hasta que ambas sean `pagado = true`.

> Verificación: 50.00 + 50.00 = **100.00 Bs** = `pedidos.total` ✓

---

### `movimientos_insumo` — consumo del pedido 2

Cálculo: `receta_detalle.cantidad × 1 unidad`

| id  | insumo_id | tipo    | cantidad | descripcion                              | pedido_id | fecha               |
| --- | --------- | ------- | -------- | ---------------------------------------- | --------- | ------------------- |
| 7   | 1         | consumo | -200.000 | Consumo pedido #2 — 1 torta de chocolate | 2         | 2026-05-04 14:05:00 |
| 8   | 2         | consumo | -150.000 | Consumo pedido #2 — 1 torta de chocolate | 2         | 2026-05-04 14:05:00 |
| 9   | 3         | consumo | -2.000   | Consumo pedido #2 — 1 torta de chocolate | 2         | 2026-05-04 14:05:00 |
| 10  | 4         | consumo | -100.000 | Consumo pedido #2 — 1 torta de chocolate | 2         | 2026-05-04 14:05:00 |
| 11  | 5         | consumo | -80.000  | Consumo pedido #2 — 1 torta de chocolate | 2         | 2026-05-04 14:05:00 |
| 12  | 6         | consumo | -50.000  | Consumo pedido #2 — 1 torta de chocolate | 2         | 2026-05-04 14:05:00 |

> El descuento de insumos ocurre al confirmar el pedido, no al pagar cada cuota.

---

### `insumos` — stock final tras ambos pedidos

| id  | nombre          | stock_inicial | consumo total | stock_actual | stock_minimo | alerta |
| --- | --------------- | ------------- | ------------- | ------------ | ------------ | ------ |
| 1   | Harina de trigo | 5000.000      | -600.000      | 4400.000     | 500          | —      |
| 2   | Azúcar          | 3000.000      | -450.000      | 2550.000     | 300          | —      |
| 3   | Huevos          | 24.000        | -6.000        | 18.000       | 6            | —      |
| 4   | Leche           | 2000.000      | -300.000      | 1700.000     | 200          | —      |
| 5   | Mantequilla     | 1000.000      | -240.000      | 760.000      | 100          | —      |
| 6   | Cacao en polvo  | 500.000       | -150.000      | 350.000      | 50           | —      |

> Todos los insumos por encima del `stock_minimo`. Sin alertas de reposición activas.

---

## Verificación cruzada del flujo

| Concepto                | Valor esperado                       | Resultado |
| ----------------------- | ------------------------------------ | --------- |
| Total pedido 1          | 2 × 100.00 = 200.00 Bs               | ✓         |
| Total pedido 2          | 1 × 100.00 = 100.00 Bs               | ✓         |
| Suma cuotas pedido 2    | 50.00 + 50.00 = 100.00 Bs            | ✓         |
| Harina consumida total  | (200×2) + (200×1) = 600 g            | ✓         |
| Huevos consumidos total | (2×2) + (2×1) = 6 u                  | ✓         |
| Cacao consumido total   | (50×2) + (50×1) = 150 g              | ✓         |
| Movimientos generados   | 6 por pedido × 2 pedidos = 12        | ✓         |
| Pedido 1 estado         | tipo contado → pagado inmediato      | ✓         |
| Pedido 2 estado         | cuota 2 pendiente → pedido pendiente | ✓         |

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

¿Seguimos con el SQL de creación de todas las tablas del sistema completo?
