# Flujo de Ejemplo con Datos Reales

Este documento ilustra cómo se interconectan los datos del sistema cuando un usuario pide un producto, cómo se descuentan los insumos y cómo se registra el pago, tanto al contado como en 2 cuotas.

## 1. Estado Inicial (Catálogo e Inventario)

### `usuarios`
| id | nombre | apellido | telefono |
|---|---|---|---|
| 1 | Juan | Pérez | 71234567 |

### `productos`
| id | nombre | precio_unitario | disponible |
|---|---|---|---|
| 10 | Torta de Chocolate | 100.00 | TRUE |

### `insumos`
| id | nombre | unidad_medida | stock_actual | stock_minimo |
|---|---|---|---|---|
| 100 | Harina de trigo | g | 5000.000 | 1000.000 |
| 101 | Azúcar | g | 3000.000 | 500.000 |
| 102 | Cacao | g | 1000.000 | 200.000 |

### `recetas`
| id | nombre | producto_id |
|---|---|---|
| 50 | Receta Torta de Chocolate | 10 |

### `receta_detalle`
Para producir **1 unidad** de Torta de Chocolate (producto `10`), se necesitan:
| id | receta_id | insumo_id | cantidad |
|---|---|---|---|
| 200 | 50 | 100 (Harina) | 500.000 |
| 201 | 50 | 101 (Azúcar) | 250.000 |
| 202 | 50 | 102 (Cacao) | 100.000 |

---

## 2. CASO A: Venta de 1 Torta al Contado (100 bs)

El usuario Juan Pérez (id `1`) compra 1 Torta de Chocolate al contado.

### `pedidos`
Se crea la cabecera del pedido.
| id | fecha | estado | total | usuario_id |
|---|---|---|---|---|
| 1001 | 2026-05-05 10:00 | entregado | 100.00 | 1 |

### `detalle_pedido`
Se enlaza el producto comprado al pedido.
| id | pedido_id | producto_id | cantidad | precio_unitario |
|---|---|---|---|---|
| 5001 | 1001 | 10 | 1 | 100.00 |

### `pagos`
Se registra el pago único al contado.
| id | fecha | tipo_pago | pedido_id |
|---|---|---|---|
| 8001 | 2026-05-05 10:05 | contado | 1001 |

*(No hay registros en `cuotas` porque fue al contado).*

### `movimientos_insumo`
El sistema busca la receta `50`, ve que necesita 500g de harina, 250g de azúcar y 100g de cacao por cada torta. Como se pidió `1` torta, genera los siguientes consumos:
| id | fecha | tipo | cantidad | insumo_id | pedido_id |
|---|---|---|---|---|---|
| 9001 | 2026-05-05 10:00 | consumo | -500.000 | 100 | 1001 |
| 9002 | 2026-05-05 10:00 | consumo | -250.000 | 101 | 1001 |
| 9003 | 2026-05-05 10:00 | consumo | -100.000 | 102 | 1001 |

### `insumos` (Nuevos saldos tras Caso A)
Se actualiza el `stock_actual` restando el consumo:
* Harina: 5000 - 500 = **4500**
* Azúcar: 3000 - 250 = **2750**
* Cacao: 1000 - 100 = **900**

---

## 3. CASO B: Venta de 1 Torta en 2 Cuotas (100 bs)

Al día siguiente, Juan Pérez (id `1`) compra otra Torta de Chocolate, pero esta vez acuerda pagar en 2 cuotas de 50 bs.

### `pedidos`
| id | fecha | estado | total | usuario_id |
|---|---|---|---|---|
| 1002 | 2026-05-06 15:00 | pendiente | 100.00 | 1 |

### `detalle_pedido`
| id | pedido_id | producto_id | cantidad | precio_unitario |
|---|---|---|---|---|
| 5002 | 1002 | 10 | 1 | 100.00 |

### `pagos`
Se registra que el pedido se pagará en cuotas.
| id | fecha | tipo_pago | pedido_id |
|---|---|---|---|
| 8002 | 2026-05-06 15:05 | cuotas | 1002 |

### `cuotas`
Al ser un pago fraccionado, el sistema genera las cuotas con sus respectivos vencimientos.
| id | numero_cuota | monto_cuota | fecha_vencimiento | fecha_pago | pagado | pago_id |
|---|---|---|---|---|---|---|
| 3001 | 1 | 50.00 | 2026-05-15 | NULL | FALSE | 8002 |
| 3002 | 2 | 50.00 | 2026-05-30 | NULL | FALSE | 8002 |
*(Cuando Juan pague, se actualizará `fecha_pago` y `pagado = TRUE` en estas filas).*

### `movimientos_insumo`
El descuento de insumos ocurre **en el momento de la venta**, sin importar que el pago sea en cuotas, ya que la torta debe ser producida igual.
| id | fecha | tipo | cantidad | insumo_id | pedido_id |
|---|---|---|---|---|---|
| 9004 | 2026-05-06 15:00 | consumo | -500.000 | 100 | 1002 |
| 9005 | 2026-05-06 15:00 | consumo | -250.000 | 101 | 1002 |
| 9006 | 2026-05-06 15:00 | consumo | -100.000 | 102 | 1002 |

### `insumos` (Nuevos saldos tras Caso B)
Se descuenta la materia prima partiendo del stock del Caso A:
* Harina: 4500 - 500 = **4000**
* Azúcar: 2750 - 250 = **2500**
* Cacao: 900 - 100 = **800**
