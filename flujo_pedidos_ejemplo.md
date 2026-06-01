# Flujo de Ejemplo con Datos Reales

Este documento ilustra cómo se interconectan los datos del sistema cuando un usuario pide un producto, cómo se descuentan los insumos y cómo se registra el pago, tanto al contado como en 2 cuotas, alineado de manera exacta a las entidades físicas de la base de datos de la repostería (Grupo 16).

---

## 1. Estado Inicial (Catálogo e Inventario)

### `roles` (Módulo 0: Accesos)
| id | nombre |
|---|---|
| 1 | Propietario |
| 2 | Secretaria |
| 3 | Cliente |

### `usuarios` (Módulo 1: Identidad)
| id | nombre | apellido | telefono | email | password | rol_id | activo |
|---|---|---|---|---|---|---|---|
| 1 | Juan | Pérez | 71234567 | juan.perez@gmail.com | $2a$10$xyz... (encriptado) | 3 (Cliente) | TRUE |

### `productos` (Módulo 1: Catálogo)
| id | nombre | descripcion | precio_unitario | disponible |
|---|---|---|---|---|
| 10 | Torta de Chocolate | Deliciosa torta artesanal con cobertura de fudge y cacao al 70% | 100.00 | TRUE |

### `insumos` (Módulo 3: Insumos)
| id | nombre | unidad_medida | stock_actual | stock_minimo | costo_unitario |
|---|---|---|---|---|---|
| 100 | Harina de trigo | g | 5000.000 | 1000.000 | 0.02 |
| 101 | Azúcar | g | 3000.000 | 500.000 | 0.01 |
| 102 | Cacao | g | 1000.000 | 200.000 | 0.05 |

### `recetas` (Módulo 3: Recetas)
| id | nombre | descripcion | creado_en | producto_id |
|---|---|---|---|---|
| 50 | Receta Torta de Chocolate | Receta estándar para tortas de chocolate del Grupo 16 | 2026-05-01 08:00:00 | 10 |

### `receta_detalle`
Para producir **1 unidad** de Torta de Chocolate (producto `10`), se necesitan:
| id | receta_id | insumo_id | cantidad |
|---|---|---|---|
| 200 | 50 | 100 (Harina) | 500.000 |
| 201 | 50 | 101 (Azúcar) | 250.000 |
| 202 | 50 | 102 (Cacao) | 100.000 |

---

## 2. CASO A: Venta de 1 Torta al Contado (100 bs)

El usuario Juan Pérez (id `1`, Cliente) compra 1 Torta de Chocolate al contado.

### `pedidos` (Módulo 2: Pedido)
Se crea la cabecera del pedido.
| id | fecha | estado | total | usuario_id |
|---|---|---|---|---|
| 1001 | 2026-05-05 10:00:00 | entregado | 100.00 | 1 |

### `detalle_pedido` (Módulo 2: Detalle)
Se enlaza el producto comprado al pedido.
| id | pedido_id | producto_id | cantidad | precio_unitario |
|---|---|---|---|---|
| 5001 | 1001 | 10 | 1 | 100.00 |

### `pagos` (Módulo 2: Pagos)
Se registra el pago único al contado.
| id | fecha | tipo_pago | pedido_id |
|---|---|---|---|
| 8001 | 2026-05-05 10:05:00 | contado | 1001 |

*(No hay registros en `cuotas` porque el tipo_pago fue 'contado').*

### `movimientos_insumo` (Módulo 3: Movimientos)
El sistema busca la receta `50`, ve que necesita 500g de harina, 250g de azúcar y 100g de cacao por cada torta. Como se pidió `1` torta, genera los siguientes consumos vinculados al pedido `1001`:
| id | fecha | tipo | cantidad | descripcion | insumo_id | pedido_id |
|---|---|---|---|---|---|---|
| 9001 | 2026-05-05 10:00:00 | consumo | -500.000 | Descuento automático por pedido #1001 | 100 | 1001 |
| 9002 | 2026-05-05 10:00:00 | consumo | -250.000 | Descuento automático por pedido #1001 | 101 | 1001 |
| 9003 | 2026-05-05 10:00:00 | consumo | -100.000 | Descuento automático por pedido #1001 | 102 | 1001 |

### `insumos` (Nuevos saldos tras Caso A)
Se actualiza el `stock_actual` restando el consumo:
* Harina: 5000.000 - 500.000 = **4500.000**
* Azúcar: 3000.000 - 250.000 = **2750.000**
* Cacao: 1000.000 - 100.000 = **900.000**

---

## 3. CASO B: Venta de 1 Torta en 2 Cuotas (100 bs)

Al día siguiente, Juan Pérez (id `1`, Cliente) compra otra Torta de Chocolate, pero esta vez acuerda pagar en 2 cuotas de 50 bs.

### `pedidos` (Módulo 2: Pedido)
| id | fecha | estado | total | usuario_id |
|---|---|---|---|---|
| 1002 | 2026-05-06 15:00:00 | pendiente | 100.00 | 1 |

### `detalle_pedido` (Módulo 2: Detalle)
| id | pedido_id | producto_id | cantidad | precio_unitario |
|---|---|---|---|---|
| 5002 | 1002 | 10 | 1 | 100.00 |

### `pagos` (Módulo 2: Pagos)
Se registra que el pedido se pagará en cuotas.
| id | fecha | tipo_pago | pedido_id |
|---|---|---|---|
| 8002 | 2026-05-06 15:05:00 | cuotas | 1002 |

### `cuotas` (Módulo 2: Cuotas)
Al ser un pago fraccionado (tipo_pago = 'cuotas'), el sistema genera las cuotas con sus respectivos vencimientos.
| id | numero_cuota | monto_cuota | fecha_vencimiento | fecha_pago | pagado | pago_id |
|---|---|---|---|---|---|---|
| 3001 | 1 | 50.00 | 2026-05-15 | NULL | FALSE | 8002 |
| 3002 | 2 | 50.00 | 2026-05-30 | NULL | FALSE | 8002 |

*(Cuando Juan realice los pagos de cada cuota, el sistema actualizará `fecha_pago` al momento actual y marcará `pagado = TRUE` en estas filas).*

### `movimientos_insumo` (Módulo 3: Movimientos)
El descuento de insumos ocurre **en el momento de la venta y generación del pedido**, sin importar que el pago sea diferido en cuotas, ya que la torta debe ser producida.
| id | fecha | tipo | cantidad | descripcion | insumo_id | pedido_id |
|---|---|---|---|---|---|---|
| 9004 | 2026-05-06 15:00:00 | consumo | -500.000 | Descuento automático por pedido #1002 | 100 | 1002 |
| 9005 | 2026-05-06 15:00:00 | consumo | -250.000 | Descuento automático por pedido #1002 | 101 | 1002 |
| 9006 | 2026-05-06 15:00:00 | consumo | -100.000 | Descuento automático por pedido #1002 | 102 | 1002 |

### `insumos` (Nuevos saldos tras Caso B)
Se descuenta la materia prima partiendo del stock del Caso A:
* Harina: 4500.000 - 500.000 = **4000.000**
* Azúcar: 2750.000 - 250.000 = **2500.000**
* Cacao: 900.000 - 100.000 = **800.000**
