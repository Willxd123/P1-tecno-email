## Casos de uso — Sistema de Gestión para “CHIFONES PERUANOS ZUZÚ”

---

### Actores

| Actor | Descripción |
|---|---|
| **Propietario** | Administrador general. Acceso total al sistema incluyendo reportes y configuración |
| **Secretario** | Operador del día a día. Gestiona pedidos, pagos, envases y clientes |
| **Cliente** | Usuario que realiza pedidos. Acceso limitado a su propia información |

---

## CU1 — Gestión de Usuarios

Administración de todos los actores del sistema: propietarios, secretarios y clientes.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU1-01 | Registrar usuario | ✓ | ✓ | — | Crear un nuevo usuario en el sistema asignándole rol: propietario, secretario o cliente |
| CU1-02 | Editar usuario | ✓ | ✓ | ✓ (solo propio) | Modificar datos personales como nombre, apellido y teléfono |
| CU1-03 | Cambiar contraseña | ✓ | ✓ | ✓ (solo propia) | Actualizar el hash de contraseña del usuario |
| CU1-04 | Desactivar usuario | ✓ | — | — | Inhabilitar un usuario sin eliminarlo del sistema |
| CU1-05 | Listar usuarios | ✓ | ✓ | — | Ver el listado completo de usuarios registrados con su rol y estado |
| CU1-06 | Buscar usuario | ✓ | ✓ | — | Buscar por nombre, apellido o teléfono para acceder rápido al perfil |
| CU1-07 | Ver perfil propio | ✓ | ✓ | ✓ | Consultar los datos personales del usuario autenticado |
| CU1-08 | Registrar rol | ✓ | — | — | Registrar un nuevo rol en el sistema |
| CU1-09 | Editar rol | ✓ | — | — | Modificar el nombre de un rol existente |
| CU1-10 | Eliminar rol | ✓ | — | — | Eliminar un rol que no esté en uso |
| CU1-11 | Listar roles | ✓ | — | — | Ver todos los roles disponibles |
| CU1-12 | Ver rol | ✓ | — | — | Consultar los detalles de un rol |

---

## CU2 — Gestión de Insumos

Control del inventario de materias primas. Incluye el catálogo, las recetas y los movimientos de stock.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU2-01 | Registrar insumo | ✓ | ✓ | — | Agregar un nuevo insumo al catálogo con nombre, unidad de medida, stock inicial, stock mínimo y costo unitario |
| CU2-02 | Editar insumo | ✓ | ✓ | — | Modificar los datos de un insumo existente como nombre, costo o stock mínimo |
| CU2-03 | Listar insumos | ✓ | ✓ | — | Ver el catálogo completo de insumos con su stock actual y estado de alerta |
| CU2-04 | Registrar entrada de stock | ✓ | ✓ | — | Registrar una compra o reposición de insumo generando un movimiento de tipo `entrada` con cantidad positiva |
| CU2-05 | Registrar ajuste de stock | ✓ | — | — | Corregir el stock de un insumo por diferencias de inventario físico generando un movimiento de tipo `ajuste` |
| CU2-06 | Registrar merma | ✓ | ✓ | — | Registrar pérdida o deterioro de un insumo generando un movimiento de tipo `merma` con cantidad negativa |
| CU2-07 | Ver historial de movimientos | ✓ | ✓ | — | Consultar todos los movimientos de un insumo: entradas, consumos, ajustes y mermas con fecha y descripción |
| CU2-08 | Ver alertas de reposición | ✓ | ✓ | — | Listar los insumos cuyo `stock_actual` está por debajo del `stock_minimo` para gestionar compras |
| CU2-09 | Registrar receta de producto | ✓ | ✓ | — | Definir los insumos y cantidades exactas que componen la receta de un producto |
| CU2-10 | Editar receta de producto | ✓ | ✓ | — | Modificar los insumos o cantidades de una receta existente |
| CU2-11 | Ver receta de producto | ✓ | ✓ | — | Consultar el detalle de insumos y cantidades que requiere un producto para producirse |

---

## CU3 — Gestión de Envases

Control de los envases reutilizables de la empresa. Incluye el stock, los préstamos por pedido y las devoluciones.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU3-01 | Registrar tipo de envase | ✓ | — | — | Agregar un nuevo tipo de envase al catálogo con nombre, descripción y stock total |
| CU3-02 | Editar tipo de envase | ✓ | — | — | Modificar los datos de un tipo de envase existente |
| CU3-03 | Ver stock de envases | ✓ | ✓ | — | Consultar por cada tipo de envase: stock total, stock disponible y cantidad actualmente prestada a clientes |
| CU3-04 | Registrar préstamo de envases | ✓ | ✓ | — | Al entregar un pedido registrar cuántos envases se prestan al cliente. Descuenta `stock_disponible` automáticamente |
| CU3-05 | Registrar devolución de envases | ✓ | ✓ | — | Al crear un nuevo pedido registrar los envases que el cliente devuelve de pedidos anteriores. Incrementa `stock_disponible` |
| CU3-06 | Ver envases pendientes por cliente | ✓ | ✓ | — | Listar todos los clientes con envases sin devolver indicando tipo, cantidad pendiente y pedido de origen |
| CU3-07 | Ver historial de envases de un cliente | ✓ | ✓ | ✓ (solo propio) | Consultar todos los préstamos y devoluciones de envases de un cliente específico con su estado actual |

---

## CU4 — Gestión de Cartilla

La cartilla es el historial digital de un cliente. Consolida sus pedidos, montos, estado de pago y estado de envases en una sola vista.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU4-01 | Ver cartilla de un cliente | ✓ | ✓ | ✓ (solo propia) | Mostrar el historial completo de pedidos del cliente con fecha, productos, monto, estado de pago y estado de envases por cada pedido |
| CU4-02 | Canjear premio de fidelización | ✓ | ✓ | ✓ (solo propio) | Reclamar un chifón tradicional gratis tras haber acumulado 10 compras y devuelto 10 envases en su cartilla |
| CU4-03 | Buscar cliente para ver cartilla | ✓ | ✓ | — | Buscar un cliente por nombre, apellido o teléfono para acceder a su cartilla |
| CU4-04 | Ver detalle de pedido en cartilla | ✓ | ✓ | ✓ (solo propio) | Desde la cartilla consultar el desglose de productos, cantidades, precios y tipo de pago de un pedido específico |
| CU4-05 | Ver estado de cuotas en cartilla | ✓ | ✓ | ✓ (solo propias) | Desde la cartilla ver el desglose de cuotas de un pedido en crédito: número, monto, vencimiento, fecha de pago y estado |
| CU4-06 | Ver estado de envases en cartilla | ✓ | ✓ | ✓ (solo propios) | Desde la cartilla ver por cada pedido cuántos envases se prestaron, cuántos se devolvieron y si hay saldo pendiente |

---

## CU5 — Gestión de Ventas

Administración del catálogo de productos disponibles para la venta.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU5-01 | Registrar producto | ✓ | ✓ | — | Agregar un nuevo producto al catálogo con nombre, descripción, precio unitario y categoría opcional |
| CU5-02 | Editar producto | ✓ | ✓ | — | Modificar nombre, descripción, precio o categoría de un producto existente |
| CU5-03 | Activar o desactivar producto | ✓ | ✓ | — | Cambiar el campo `disponible` para ocultar o mostrar un producto sin eliminarlo |
| CU5-04 | Listar productos disponibles | ✓ | ✓ | ✓ | Ver el catálogo de productos activos con nombre, descripción, precio y categoría |
| CU5-05 | Ver costo de producción de producto | ✓ | — | — | Calcular el costo total de insumos que requiere producir una unidad del producto en base a su receta y `costo_unitario` de cada insumo |
| CU5-06 | Verificar disponibilidad de insumos | ✓ | ✓ | — | Antes de confirmar un pedido verificar si hay stock suficiente de todos los insumos de la receta para la cantidad solicitada |
| CU5-07 | Registrar categoría | ✓ | — | — | Crear una nueva categoría para agrupar los productos |
| CU5-08 | Listar categorías | ✓ | ✓ | ✓ | Ver el listado de todas las categorías de producto registradas |

---

## CU6 — Gestión de Pedidos

Ciclo de vida completo de un pedido desde su creación hasta su entrega.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU6-01 | Crear pedido | ✓ | ✓ | — | Registrar un nuevo pedido para un cliente seleccionando productos y cantidades. El sistema calcula el total y descuenta insumos automáticamente |
| CU6-02 | Ver detalle de pedido | ✓ | ✓ | ✓ (solo propios) | Consultar los productos, cantidades, precios unitarios y total de un pedido específico |
| CU6-03 | Listar pedidos | ✓ | ✓ | ✓ (solo propios) | Ver el listado de pedidos filtrable por cliente, fecha, estado y tipo de pago |
| CU6-04 | Cambiar estado de pedido | ✓ | ✓ | — | Actualizar el estado del pedido entre `pendiente`, `pagado`, `entregado` o `cancelado` |
| CU6-05 | Cancelar pedido | ✓ | ✓ | — | Cancelar un pedido pendiente. El sistema revierte el consumo de insumos generando movimientos de tipo `ajuste` |
| CU6-06 | Confirmar entrega de pedido | ✓ | ✓ | — | Marcar el pedido como `entregado` y en ese momento registrar el préstamo de envases si corresponde |

---

## CU7 — Gestión de Pagos

Registro y seguimiento de pagos al contado y en crédito con 2 o más cuotas.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU7-01 | Registrar pago de una cuota | ✓ | ✓ | — | Marcar una cuota específica como pagada registrando la `fecha_pago` y actualizando `pagado = true`. Si es la última cuota el pedido pasa a `pagado` |
| CU7-02 | Ver estado de cuotas de un pedido | ✓ | ✓ | ✓ (solo propias) | Consultar el plan de cuotas de un pedido en crédito: número de cuota, monto, fecha de vencimiento, fecha de pago y estado |
| CU7-03 | Ver cuotas vencidas | ✓ | ✓ | — | Listar todas las cuotas cuya `fecha_vencimiento` ya pasó y `pagado = false`, agrupadas por cliente para gestionar cobros |
| CU7-04 | Registrar pago al contado | ✓ | ✓ | — | Al confirmar el pedido registrar el pago completo inmediato. El pedido pasa automáticamente a estado `pagado` |
| CU7-05 | Registrar pago en cuotas | ✓ | ✓ | — | Al confirmar el pedido definir el número de cuotas, el monto de cada una y las fechas de vencimiento. Se generan los registros en `cuotas` |
| CU7-06 | Ver cuotas próximas a vencer | ✓ | ✓ | — | Listar las cuotas que vencen en los próximos N días para anticipar cobros y notificar clientes |
| CU7-07 | Ver resumen de pagos de un cliente | ✓ | ✓ | ✓ (solo propio) | Consultar el total pagado, el total pendiente y el detalle de pagos de un cliente específico |

---

## CU8 — Reportes y Estadísticas

Consultas gerenciales para toma de decisiones. Solo disponibles para el propietario.

| Código | Nombre | Propietario | Secretario | Cliente | Descripción |
|---|---|---|---|---|---|
| CU8-01 | Reporte de ventas por período | ✓ | — | — | Total de pedidos, monto vendido y productos más vendidos en un rango de fechas |
| CU8-02 | Reporte de ingresos contado vs crédito | ✓ | — | — | Comparativa de montos cobrados al contado versus en cuotas en un período determinado |
| CU8-03 | Reporte de cuotas pendientes | ✓ | — | — | Monto total adeudado por todos los clientes con crédito activo, detallado por cliente |
| CU8-04 | Reporte de consumo de insumos | ✓ | — | — | Cantidad total consumida de cada insumo en un período, útil para planificar compras |
| CU8-05 | Reporte de costo de producción | ✓ | — | — | Costo total de insumos consumidos versus ingresos generados en un período para calcular margen |
| CU8-06 | Reporte de stock crítico de insumos | ✓ | ✓ | — | Listado de insumos por debajo del stock mínimo con cantidad faltante para alcanzar el umbral |
| CU8-07 | Reporte de envases prestados | ✓ | — | — | Total de envases actualmente fuera de la repostería por tipo, con detalle por cliente y antigüedad del préstamo |
| CU8-08 | Reporte de clientes frecuentes | ✓ | — | — | Ranking de clientes por número de pedidos y monto total comprado en un período |
| CU8-09 | Reporte de productos más vendidos | ✓ | — | — | Ranking de productos por unidades vendidas y monto generado en un período |

## Matriz general de acceso

| Módulo | Propietario | Secretario | Cliente |
|---|---|---|---|
| CU1 Gestión de usuarios y roles | Acceso total | Registrar y editar clientes | Solo perfil propio |
| CU2 Gestión de insumos | Acceso total | Sin ajustes manuales de stock | Sin acceso |
| CU3 Gestión de envases | Acceso total | Sin crear tipos de envase | Solo historial propio |
| CU4 Gestión de cartilla | Acceso total | Acceso total | Solo cartilla propia |
| CU5 Gestión de ventas | Acceso total | Sin ver costos de producción | Solo ver catálogo |
| CU6 Gestión de pedidos | Acceso total | Acceso total | Solo ver propios |
| CU7 Gestión de pagos | Acceso total | Acceso total | Solo ver propios |
| CU8 Reportes y estadísticas | Acceso total | Solo stock crítico | Sin acceso |