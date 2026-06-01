# Implementación del Sistema TecnoEmailZUZU — Grupo 16

**Proyecto:** Sistema de gestión de repostería operado exclusivamente por correo electrónico.
**Tecnología:** Java 8 puro (sin frameworks), PostgreSQL, SMTP/POP3 via sockets TCP.
**Correo del sistema:** `grupo16sc@tecnoweb.org.bo`

---

## Arquitectura General

```
src/
├── Main.java                                  # Punto de entrada
├── configuracion/
│   ├── Configuracion.java                     # Variables de entorno (.env)
│   ├── schema.sql                             # DDL — 13 tablas (CREATE IF NOT EXISTS)
│   └── seeder.sql                             # Datos de prueba (ON CONFLICT DO NOTHING)
├── CapaDatos/                                 # Capa 1: CRUD directo a PostgreSQL via JDBC
├── CapaNegocio/                               # Capa 2: Lógica de dominio y validaciones
├── CapaPresentacion/                          # Capa 3: Generación de HTML para correo
│   └── controllers/                           # Enrutadores de comandos
└── utils/                                     # Infraestructura (POP3, SMTP, parser)
```

### Flujo de procesamiento de un correo

```
[Correo del usuario]
        │
        ▼
[POP3 :110] ← MailVerificationThread cada 10 seg
        │
        ▼
[Extractor] → From + Subject
        │
        ▼
[Analex] → comando="CU6_01"  params=["1","contado","10:2"]
        │
        ▼
[ejecutarComando()] → UsuarioController / InsumoController / PedidoController / ...
        │
        ▼
[NXxx.metodo(params)] → lógica + base de datos
        │
        ▼
[PXxx.generarHtml()] → HTML con CSS inline (tema caramel/chocolate)
        │
        ▼
[SMTP :25] → respuesta al remitente
```

---

## Base de Datos — 13 Tablas

| Tabla | Módulo | Descripción |
|-------|--------|-------------|
| `roles` | M0 | Propietario, Secretaria, Cliente |
| `usuarios` | M1 | Usuarios del sistema con campo `activo` |
| `productos` | M1 | Catálogo de chifones y tortas con `disponible` |
| `pedidos` | M2 | Cabecera de pedidos (estado: pendiente/pagado/cancelado/entregado) |
| `detalle_pedido` | M2 | Líneas de cada pedido (producto, cantidad, precio) |
| `pagos` | M2 | Tipo de pago (contado/cuotas) por pedido |
| `cuotas` | M2 | Cuotas del pago fraccionado con fechas de vencimiento |
| `insumos` | M3 | Materias primas con stock_actual, stock_minimo, costo_unitario |
| `recetas` | M3 | Receta 1:1 con producto |
| `receta_detalle` | M3 | Insumos y cantidades por receta |
| `movimientos_insumo` | M3 | Historial de entradas/consumos/ajustes/mermas |
| `envases` | M4 | Envases reutilizables (stock_total, stock_disponible) |
| `pedido_envase` | M4 | Préstamos y devoluciones de envases por pedido |

---

## Casos de Uso Implementados

### ✅ CU1 — Gestión de Usuarios

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU1_01` | `INSPER`, `REGISTRAR_USUARIO` | Nombre, Apellido, Tel, Email, Pass, Rol | Registrar usuario |
| `CU1_02` | `EDITAR_USUARIO` | ID, Nombre, Apellido, Tel, Email | Editar datos |
| `CU1_03` | `CAMBIAR_PASSWORD` | ID, NuevaPass | Cambiar contraseña |
| `CU1_04` | `DESACTIVAR_USUARIO` | ID | Desactivar (soft delete) |
| `CU1_05` | `LISPER`, `LISTAR_USUARIOS` | — | Listar usuarios |
| `CU1_06` | `BUSCAR_USUARIO` | Texto | Buscar por nombre/apellido/teléfono |
| `CU1_07` | `VER_PERFIL` | ID | Ver perfil completo |

> **Roles válidos:** Propietario, Secretaria, Cliente
> **Contraseña:** hasheada con SHA-256

---

### ✅ CU2 — Gestión de Insumos y Recetas

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU2_01` | `REGINSM` | nombre, unidad, stock_ini, stock_min, costo | Registrar insumo |
| `CU2_02` | `EDTINSM` | id, nombre, costo, stock_min | Editar insumo |
| `CU2_03` | `LISINSM` | — | Listar insumos con estado (OK/CRÍTICO) |
| `CU2_04` | `ENTINSM` | insumo_id, cantidad, descripcion | Entrada de stock |
| `CU2_05` | `AJUINSM` | insumo_id, cant_nueva, descripcion | Ajuste de stock |
| `CU2_06` | `MERINSM` | insumo_id, cantidad, descripcion | Registrar merma |
| `CU2_07` | `HISINSM` | insumo_id | Historial de movimientos |
| `CU2_08` | `ALEREP` | — | Alertas de reposición (stock < mínimo) |
| `CU2_09` | `REGREC` | prod_id, nombre, desc, ins_id:cant, ... | Registrar receta |
| `CU2_10` | `EDTREC` | receta_id, ins_id:cant, ... | Reemplazar receta |
| `CU2_11` | `VERREC` | producto_id | Ver receta e ingredientes |

> **Unidades válidas:** `kg`, `g`, `l`, `ml`, `unidad`
> **Movimientos:** siempre registran el historial y actualizan `stock_actual` automáticamente.

---

### ✅ CU3 — Gestión de Envases

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU3_01` | `REGENV` | nombre, descripcion, stock_total | Registrar tipo de envase |
| `CU3_02` | `EDTENV` | id, nombre, descripcion | Editar envase |
| `CU3_03` | `LISENV` | — | Stock total, disponible y prestados |
| `CU3_04` | `PRESENV` | pedido_id, envase_id, cantidad | Registrar préstamo |
| `CU3_05` | `DEVENV` | pedido_origen_id, envase_id, cantidad | Registrar devolución |
| `CU3_06` | `PENDENV` | — | Envases sin devolver por cliente |
| `CU3_07` | `HISENV` | usuario_id | Historial de envases de un cliente |

> Al prestar: `stock_disponible -= cantidad`. Al devolver: `stock_disponible += cantidad`.

---

### ✅ CU4 — Cartilla del Cliente

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU4_01` | `CARTILLA` | usuario_id | Historial completo de pedidos del cliente |
| `CU4_02` | `BUSCART` | texto | Buscar cliente para ver su cartilla |
| `CU4_03` | `DETCART` | pedido_id | Desglose de productos y precios de un pedido |
| `CU4_04` | `CUOTACART` | pedido_id | Estado de cuotas de un pedido |
| `CU4_05` | `ENVCART` | pedido_id | Envases prestados/devueltos de un pedido |

---

### ✅ CU5 — Gestión de Productos (Ventas)

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU5_01` | `REGPRO` | nombre, descripcion, precio_unitario | Registrar producto |
| `CU5_02` | `EDTPRO` | id, nombre, descripcion, precio | Editar producto |
| `CU5_03` | `TOGPRO` | id | Activar / desactivar del catálogo |
| `CU5_04` | `LISPRO` | — | Listar todos los productos |
| `CU5_05` | `COSTPRO` | producto_id | Costo de producción y margen |
| `CU5_06` | `DISPRO` | producto_id, cantidad | Verificar stock de insumos disponible |

---

### ✅ CU6 — Gestión de Pedidos

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU6_01` | `CRPEDIDO` | ver formato abajo | Crear pedido (complejo) |
| `CU6_02` | `DETPEDIDO` | pedido_id | Ver detalle completo del pedido |
| `CU6_03` | `LISPEDIDO` | [filtro] | Listar pedidos (opcional: usuario_id o estado) |
| `CU6_04` | `EDTPEDIDO` | pedido_id, estado | Cambiar estado manualmente |
| `CU6_05` | `CELPEDIDO` | pedido_id | Cancelar pedido (restaura stock) |
| `CU6_06` | `ENTPEDIDO` | pedido_id | Confirmar entrega |

#### Formato de CRPEDIDO

```
# Pago al contado:
CU6_01["usuario_id","contado","prod_id:cantidad","prod_id:cantidad",...]

Ejemplo: CU6_01["1","contado","10:2","11:1"]

# Pago en cuotas:
CU6_01["usuario_id","cuotas","num_cuotas","fecha1;fecha2;...","prod_id:cantidad",...]

Ejemplo: CU6_01["1","cuotas","2","2026-07-01;2026-08-01","10:1"]
```

#### Flujo interno de CRPEDIDO

1. Valida que el usuario y productos existen y están activos
2. Verifica stock de insumos para cada producto (via receta)
3. Calcula el total
4. `INSERT pedidos` (estado = pendiente)
5. `INSERT detalle_pedido` por cada producto
6. Por cada producto → busca receta → `INSERT movimientos_insumo` (tipo: consumo) + `UPDATE insumos.stock_actual`
7. `INSERT pagos` (contado o cuotas)
8. Si cuotas → `INSERT cuotas` (N registros con fechas de vencimiento)

#### Flujo de CELPEDIDO

1. Verifica que el pedido existe y está en estado `pendiente`
2. Por cada detalle → busca receta → `INSERT movimientos_insumo` (tipo: ajuste, cantidad positiva)
3. `UPDATE insumos.stock_actual` (restauración)
4. `UPDATE pedidos.estado = 'cancelado'`

---

### ✅ CU7 — Gestión de Pagos

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU7_01` | `PAGCONTADO` | pedido_id | Confirmar pago al contado → estado: pagado |
| `CU7_02` | `PAGCUOTAS` | pedido_id, num_cuotas, fechas (;) | Configurar/reconfigurar cuotas |
| `CU7_03` | `PAGCUOTA` | cuota_id | Marcar cuota como pagada |
| `CU7_04` | `VISCUOTAS` | pedido_id | Ver plan de cuotas de un pedido |
| `CU7_05` | `CUOVENC` | — | Cuotas vencidas y sin pagar |
| `CU7_06` | `CUOPROX` | dias | Cuotas que vencen en los próximos N días |
| `CU7_07` | `RESPAGCLI` | usuario_id | Resumen de pagos de un cliente |

> PAGCUOTA: cuando todas las cuotas del pedido están pagadas → `pedidos.estado = 'pagado'` automáticamente.

---

### ✅ CU8 — Reportes y Estadísticas

| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU8_01` | `REPVENTAS` | fecha_ini, fecha_fin | Total de ventas por período |
| `CU8_02` | `REPINGRES` | fecha_ini, fecha_fin | Ingresos contado vs crédito |
| `CU8_03` | `REPCUOPEND` | — | Monto total de cuotas pendientes |
| `CU8_04` | `REPCONINS` | fecha_ini, fecha_fin | Consumo de insumos por período |
| `CU8_05` | `REPCOSTO` | fecha_ini, fecha_fin | Costo producción vs ingresos (margen) |
| `CU8_06` | `REPSTOCK` | — | Insumos con stock crítico |
| `CU8_07` | `REPENVPRES` | — | Envases actualmente prestados |
| `CU8_08` | `REPCLIFRE` | fecha_ini, fecha_fin | Ranking de clientes frecuentes |
| `CU8_09` | `REPPROVEND` | fecha_ini, fecha_fin | Ranking de productos más vendidos |

> Formato de fechas: `YYYY-MM-DD`

---

### ✅ Gestión de Roles (auxiliar)

| Comando | Alias | Parámetros |
|---------|-------|------------|
| `REGROL` | `REGISTRAR_ROL` | NombreRol |
| `EDTROL` | `EDITAR_ROL` | ID, NuevoNombre |
| `DELROL` | `ELIMINAR_ROL` | ID |
| `LISROL` | `LISTAR_ROLES` | — |
| `VERROL` | `VER_ROL` | ID |

---

### ✅ Ayuda

| Comando | Descripción |
|---------|-------------|
| `HELP` / `AYUDA` | Devuelve HTML con tabla completa de todos los comandos organizados por CU |

---

## Manejo de Errores

Cada comando valida sus parámetros antes de ejecutar. Ejemplos de mensajes:

| Situación | Mensaje devuelto |
|-----------|-----------------|
| Asunto vacío | `Error: Asunto vacío. Por favor envíe un comando válido.` |
| Comando desconocido | `Error: Comando 'XYZ' no reconocido. Envíe 'HELP' para ver los comandos.` |
| Parámetros insuficientes | `Error: Se requieren 3 parámetros: nombre, descripcion, precio_unitario.` |
| ID no numérico | `Error: El ID debe ser numérico.` |
| Registro no encontrado | `Error: No existe un producto con ID 99.` |
| Unidad inválida | `Error: Unidad de medida inválida. Valores válidos: kg, g, l, ml, unidad.` |
| Stock insuficiente | `Error: Stock insuficiente de 'Harina de trigo'. Disponible: 200g, Necesario: 500g` |
| Tipo de pago inválido | `Error: Tipo de pago inválido. Use 'contado' o 'cuotas'.` |
| Fecha con formato malo | `Error: Formato de fecha inválido. Use YYYY-MM-DD.` |

---

## Archivos del Proyecto

### Capa de Datos (`src/CapaDatos/`)

| Archivo | Tabla | Métodos clave |
|---------|-------|---------------|
| `Conexion.java` | — | Singleton JDBC |
| `DatabaseInitializer.java` | — | Ejecuta schema.sql + seeder.sql |
| `DRoles.java` | `roles` | CRUD + obtenerPorNombre |
| `DUsuarios.java` | `usuarios` | CRUD + obtenerPorId + obtenerPorEmail + listar |
| `DProductos.java` | `productos` | CRUD + obtenerPorId + listar + listarDisponibles |
| `DPedidos.java` | `pedidos` | CRUD + listar + listarPorUsuario + listarPorEstado + listarPorFechas |
| `DDetallePedido.java` | `detalle_pedido` | CRUD + listarPorPedido |
| `DPagos.java` | `pagos` | CRUD + obtenerPorPedido + obtenerPorId |
| `DCuotas.java` | `cuotas` | CRUD + listarPorPago + obtenerPorId + listarVencidas + listarProximasAVencer |
| `DInsumos.java` | `insumos` | CRUD + obtenerPorId + listar + listarStockCritico |
| `DRecetas.java` | `recetas` | CRUD + obtenerPorProducto + listar |
| `DRecetaDetalle.java` | `receta_detalle` | CRUD + listarPorReceta |
| `DMovimientosInsumo.java` | `movimientos_insumo` | CRUD + listarPorInsumo + listarPorPedido + listarTodos |
| `DEnvases.java` | `envases` | CRUD + obtenerPorId + obtenerPorNombre + listar |
| `DPedidoEnvase.java` | `pedido_envase` | CRUD + listarPorPedidoOrigen + listarPorPedidoDevolucion + listarPendientes + listarPorUsuario |

### Enums (`src/CapaDatos/enums/`)

| Enum | Valores |
|------|---------|
| `RolNombre` | Propietario, Secretaria, Cliente |
| `EstadoPedido` | pendiente, pagado, cancelado, entregado |
| `TipoPago` | contado, cuotas |
| `UnidadMedida` | kg, g, l, ml, unidad |
| `TipoMovimientoInsumo` | entrada, consumo, ajuste, merma |

### Capa de Negocio (`src/CapaNegocio/`)

| Archivo | CU | Descripción |
|---------|-----|-------------|
| `NRoles.java` | Aux | Gestión de roles |
| `NUsuarios.java` | CU1 | Usuarios: registro, edición, búsqueda |
| `NProductos.java` | CU5 | Catálogo: registro, edición, costo |
| `NInsumos.java` | CU2 | Insumos: movimientos de stock, recetas |
| `NPedidos.java` | CU6 | Pedidos: creación, cancelación, entrega |
| `NPagos.java` | CU7 | Pagos: contado, cuotas, cobros |
| `NEnvases.java` | CU3 | Envases: préstamos, devoluciones |
| `NCartilla.java` | CU4 | Cartilla: historial consolidado del cliente |
| `NReportes.java` | CU8 | Reportes gerenciales por período |

### Capa de Presentación (`src/CapaPresentacion/`)

| Archivo | Descripción |
|---------|-------------|
| `PlantillaBase.java` | Template HTML compartido con CSS inline, badge system, parser ASCII→tabla |
| `PRoles.java` | HTML para respuestas de Roles |
| `PUsuarios.java` | HTML para respuestas de Usuarios |
| `PProductos.java` | HTML para respuestas de Productos |
| `PInsumos.java` | HTML para respuestas de Insumos |
| `PPedidos.java` | HTML para respuestas de Pedidos |
| `PPagos.java` | HTML para respuestas de Pagos |
| `PEnvases.java` | HTML para respuestas de Envases |
| `PCartilla.java` | HTML para respuestas de Cartilla |
| `PReportes.java` | HTML para respuestas de Reportes |

### Controllers (`src/CapaPresentacion/controllers/`)

| Archivo | Comandos que maneja |
|---------|-------------------|
| `RolController.java` | REGROL, EDTROL, DELROL, LISROL, VERROL |
| `UsuarioController.java` | CU1_01…CU1_07 + aliases |
| `ProductoController.java` | CU5_01…CU5_06 + aliases |
| `InsumoController.java` | CU2_01…CU2_11 + aliases |
| `PedidoController.java` | CU6_01…CU6_06 + aliases |
| `PagoController.java` | CU7_01…CU7_07 + aliases |
| `EnvaseController.java` | CU3_01…CU3_07 + aliases |
| `CartillaController.java` | CU4_01…CU4_05 + aliases |
| `ReporteController.java` | CU8_01…CU8_09 + aliases |

### Utilidades (`src/utils/`)

| Archivo | Descripción |
|---------|-------------|
| `MailVerificationThread.java` | Hilo principal: POP3 → Analex → Controller → SMTP. Ciclo cada 10 seg. |
| `Command.java` | Comandos POP3 (USER, PASS, STAT, RETR, DELE, QUIT) |
| `Email.java` | POJO: from, subject, body |
| `Extractor.java` | Parsea raw email → Email object |
| `analex/Analex.java` | Parser léxico: extrae comando y parámetros del Subject |
| `validadores/UsuarioValidator.java` | Validaciones de nombre, teléfono, password, rol |

---

## Compilación y Ejecución

```powershell
# Desde la raíz TecnoEmailZUZU/

# Compilar
javac --release 8 -d bin -cp "lib/*" -sourcepath src src/Main.java

# Ejecutar
java -cp "bin;lib/*" Main
```

### Dependencias (`lib/`)
- `postgresql-42.7.3.jar` — Driver JDBC PostgreSQL
- `javax.mail-1.6.2.jar` — Presente pero no usado (se usan sockets TCP puros)

### Configuración (`.env`)
```
DB_HOST=tecnoweb.org.bo
DB_PORT=5432
DB_NAME=db_grupo16sc
DB_USER=grupo16sc
DB_PASS=...
SMTP_HOST=mail.tecnoweb.org.bo
SMTP_PORT=25
SMTP_MAIL=grupo16sc@tecnoweb.org.bo
POP3_HOST=mail.tecnoweb.org.bo
POP3_PORT=110
POP3_USER=grupo16sc
POP3_PASS=...
```

---

## Notas Importantes

1. **Comandos usan guion bajo `_`**, nunca guion medio `-`. El parser Analex no soporta guiones en identificadores. Usar `CU6_01`, no `CU6-01`.
2. **Autenticación desactivada** temporalmente en `MailVerificationThread`: cualquier correo puede ejecutar comandos sin estar registrado.
3. **Los correos se eliminan** del servidor POP3 después de procesarse (comando DELE).
4. **HTML con CSS inline**: Gmail ignora CSS externo y `<style>` en `<head>`. Toda la presentación usa `style=""` inline.
5. **Descuento de insumos automático**: ocurre al crear el pedido, no al momento del pago. Si no hay receta para un producto, el pedido se crea sin descontar insumos.
6. **Restauración automática** al cancelar pedido: genera movimientos tipo `ajuste` por cada insumo consumido.
7. **Bug corregido**: `DCuotas.insertar()` manejaba `fecha_pago = null` con NullPointerException. Ahora usa `ps.setNull(4, Types.DATE)`.
