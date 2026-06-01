# CLAUDE.md — Contexto del Proyecto TecnoEmailZUZU (Grupo 16)

## 🎯 ¿Qué es este proyecto?

Es un **sistema de gestión para una repostería** que fabrica **chifones peruanos** de varios sabores.
Toda la interacción se realiza **exclusivamente por correo electrónico**: los usuarios envían comandos como asunto de un email a `grupo16sc@tecnoweb.org.bo`, el sistema procesa el comando contra una base de datos PostgreSQL y responde automáticamente con el resultado formateado en **HTML premium**.

**NO tiene interfaz web ni API REST.** El correo electrónico ES la interfaz.

---

## 🏗️ Arquitectura

### Arquitectura de 3 Capas (Puro Java 8+, sin frameworks)

```
src/
├── Main.java                          # Punto de entrada: inicia DatabaseInitializer + MailVerificationThread
├── configuracion/
│   ├── Configuracion.java             # Lee variables de entorno desde .env (DB, SMTP, POP3)
│   ├── schema.sql                     # DDL: CREATE TABLE IF NOT EXISTS (idempotente)
│   └── seeder.sql                     # DML: Datos iniciales (ON CONFLICT DO NOTHING)
├── CapaDatos/                         # CAPA DE DATOS (Acceso directo a PostgreSQL via JDBC)
│   ├── Conexion.java                  # Singleton de conexión JDBC
│   ├── DatabaseInitializer.java       # Ejecuta schema.sql + seeder.sql al arrancar
│   ├── DRoles.java                    # CRUD tabla roles
│   ├── DUsuarios.java                 # CRUD tabla usuarios
│   ├── DProductos.java                # CRUD tabla productos
│   ├── DPedidos.java                  # CRUD tabla pedidos
│   ├── DDetallePedido.java            # CRUD tabla detalle_pedido
│   ├── DPagos.java                    # CRUD tabla pagos
│   ├── DCuotas.java                   # CRUD tabla cuotas
│   ├── DInsumos.java                  # CRUD tabla insumos
│   ├── DRecetas.java                  # CRUD tabla recetas
│   ├── DRecetaDetalle.java            # CRUD tabla receta_detalle
│   ├── DMovimientosInsumo.java        # CRUD tabla movimientos_insumo
│   ├── DEnvases.java                  # CRUD tabla envases
│   ├── DPedidoEnvase.java             # CRUD tabla pedido_envase
│   └── enums/
│       ├── RolNombre.java             # Propietario, Secretaria, Cliente
│       ├── EstadoPedido.java          # pendiente, pagado, cancelado, entregado
│       ├── TipoPago.java              # contado, cuotas
│       ├── UnidadMedida.java          # kg, g, l, ml, unidad
│       └── TipoMovimientoInsumo.java  # entrada, consumo, ajuste, merma
├── CapaNegocio/                       # CAPA DE NEGOCIO (Validaciones + lógica de dominio)
│   ├── NUsuarios.java                 # Lógica CU1 (Gestión de Usuarios)
│   └── NRoles.java                    # Lógica auxiliar de Roles
├── CapaPresentacion/                  # CAPA DE PRESENTACIÓN (Generación de HTML para email)
│   ├── PUsuarios.java                 # Genera HTML premium para respuestas de Usuarios
│   ├── PRoles.java                    # Genera HTML premium para respuestas de Roles
│   └── controllers/
│       ├── UsuarioController.java     # Enruta comandos CU1_XX a NUsuarios
│       └── RolController.java         # Enruta comandos REGROL/EDTROL/etc a NRoles
└── utils/
    ├── MailVerificationThread.java     # HILO PRINCIPAL: POP3 → Analex → Controller → SMTP
    ├── Command.java                   # Helper de comandos POP3 (USER, PASS, STAT, RETR, DELE, QUIT)
    ├── Email.java                     # POJO: from, subject, body
    ├── Extractor.java                 # Parsea raw email text → Email object
    ├── analex/
    │   └── Analex.java                # Analizador léxico: extrae comando y parámetros del Subject
    └── validadores/
        └── UsuarioValidator.java      # Validaciones de entrada para datos de usuario
```

### Flujo de procesamiento de un correo

```
[Gmail del usuario]
       │
       ▼
[POP3: mail.tecnoweb.org.bo:110] ← MailVerificationThread lee correos cada 10 seg
       │
       ▼
[Extractor.getEmail()] → extrae From y Subject del correo crudo
       │
       ▼
[Analex.getComando() + getParametros()] → parsea "CU1_01["Juan","Perez",...]"
       │                                    comando = "CU1_01"
       │                                    params = ["Juan", "Perez", ...]
       ▼
[ejecutarComando()] → enruta al Controller correcto
       │
       ├── UsuarioController.canHandle() → NUsuarios.método() → DUsuarios.operacion()
       ├── RolController.canHandle()     → NRoles.método()    → DRoles.operacion()
       └── "HELP" / "AYUDA"             → obtenerAyuda()
       │
       ▼
[PUsuarios.generarHtml() / PRoles.generarHtml()] → HTML premium con CSS inline
       │
       ▼
[SMTP: mail.tecnoweb.org.bo:25] → envía respuesta al usuario vía socket TCP puro
```

---

## 📧 Formato de Comandos por Correo

Los usuarios envían correos a `grupo16sc@tecnoweb.org.bo` con el comando en el **Asunto (Subject)**.

> ⚠️ **IMPORTANTE:** En los comandos de asunto se usa **guion bajo `_`** y **NO guion medio `-`** porque el parser (Analex) no soporta guiones medios en los identificadores de comando. Ejemplo: usar `CU1_01`, NO `CU1-01`.

### Formato del Subject
```
COMANDO["param1","param2","param3"]
```

### Formatos alternativos soportados por Analex
```
COMANDO [param1] [param2] [param3]    # Multi-corchete
COMANDO["param1", "param2"]           # Con espacios
COMANDO[param1, param2]               # Sin comillas
LISTAR_USUARIOS                        # Sin parámetros
```

---

## 📋 Casos de Uso (Estado de Implementación)

### ✅ CU1 — Gestión de Usuarios (IMPLEMENTADO)
| Comando | Alias | Parámetros | Descripción |
|---------|-------|------------|-------------|
| `CU1_01` | `REGISTRAR_USUARIO`, `INSPER` | Nombre, Apellido, Telefono, Email, Password, Rol | Registrar nuevo usuario |
| `CU1_02` | `EDITAR_USUARIO` | ID, Nombre, Apellido, Telefono, Email | Editar datos de usuario |
| `CU1_03` | `CAMBIAR_PASSWORD` | ID, NuevaPassword | Cambiar contraseña |
| `CU1_04` | `DESACTIVAR_USUARIO` | ID | Desactivar usuario (soft delete con campo `activo`) |
| `CU1_05` | `LISTAR_USUARIOS`, `LISPER` | (ninguno) | Listar todos los usuarios |
| `CU1_06` | `BUSCAR_USUARIO` | TextoABuscar | Buscar por nombre/apellido/email |
| `CU1_07` | `VER_PERFIL` | ID | Ver perfil de un usuario |

### ✅ Gestión de Roles (IMPLEMENTADO - Auxiliar)
| Comando | Alias | Parámetros |
|---------|-------|------------|
| `REGROL` | `REGISTRAR_ROL` | NombreRol |
| `EDTROL` | `EDITAR_ROL` | ID, NuevoNombre |
| `DELROL` | `ELIMINAR_ROL` | ID |
| `LISROL` | `LISTAR_ROLES` | (ninguno) |
| `VERROL` | `VER_ROL` | ID |

### ❌ CU2 — Gestión de Insumos y Recetas (PENDIENTE)
Incluye: productos, insumos, recetas, receta_detalle, movimientos_insumo.

### ❌ CU3 — Gestión de Envases (PENDIENTE)
Control de envases reutilizables (préstamo y devolución). Los envases van y vuelven con cada pedido. Se controla stock_total vs stock_disponible.

### ❌ CU4 — Cartilla del Cliente (PENDIENTE)
Historial digital consolidado de pedidos, pagos y envases de un cliente.

### ❌ CU5 — Gestión de Ventas (PENDIENTE)
Catálogo público y costos de producción.

### ❌ CU6 — Gestión de Pedidos (PENDIENTE)
Crear, confirmar, entregar y cancelar pedidos. Descontar insumos automáticamente según receta.

### ❌ CU7 — Gestión de Pagos (PENDIENTE)
Pagos al contado, cuotas, registro de cobros.

### ❌ CU8 — Reportes y Estadísticas (PENDIENTE)
Reportes gerenciales: ventas, ingresos, cuotas, stock crítico, envases, clientes frecuentes.

---

## 🗄️ Base de Datos (PostgreSQL)

### Tablas existentes (13 tablas)
| Tabla | Módulo | Descripción |
|-------|--------|-------------|
| `roles` | M0 | Propietario, Secretaria, Cliente |
| `usuarios` | M1 | Usuarios con campos: id, nombre, apellido, telefono, email, password, rol_id, **activo** |
| `productos` | M1 | Catálogo de productos (chifones, tortas) |
| `pedidos` | M2 | Cabecera de pedidos (estado: pendiente/pagado/cancelado/entregado) |
| `detalle_pedido` | M2 | Líneas de cada pedido (producto, cantidad, precio) |
| `pagos` | M2 | Registro de pago (tipo: contado/cuotas) |
| `cuotas` | M2 | Cuotas de pagos fraccionados |
| `insumos` | M3 | Materias primas (harina, azúcar, cacao, huevos, etc.) |
| `recetas` | M3 | Receta vinculada 1:1 a un producto |
| `receta_detalle` | M3 | Insumos y cantidades por receta |
| `movimientos_insumo` | M3 | Historial de entradas/consumos/ajustes/mermas |
| `envases` | M4 | Envases reutilizables (moldes de chifón, tuppers) con stock_total y stock_disponible |
| `pedido_envase` | M4 | Préstamos y devoluciones de envases por pedido |

### Conexión
Las credenciales se leen desde `.env` vía `configuracion/Configuracion.java`:
- **Producción:** `tecnoweb.org.bo:5432` / `grupo16sc` / `db_grupo16sc`
- **Local dev:** `localhost:5432` / credenciales locales / `tecno_dev`

### Inicialización automática
Al ejecutar `Main.java`, `DatabaseInitializer` ejecuta secuencialmente:
1. `schema.sql` — Crea tablas con `IF NOT EXISTS` (idempotente)
2. `seeder.sql` — Inserta datos de prueba con `ON CONFLICT DO NOTHING` (idempotente)

**IMPORTANTE:** `CREATE TABLE IF NOT EXISTS` NO modifica tablas existentes. Si se necesita agregar una columna a una tabla ya creada, se debe ejecutar `ALTER TABLE` manualmente o dropear y recrear.

---

## 🧩 Patrones de Código a Seguir

### Para agregar un nuevo Caso de Uso (ej: CU6 — Pedidos)

1. **CapaDatos:** Ya existe `DPedidos.java`. Verificar que tenga todos los métodos CRUD necesarios.
2. **CapaNegocio:** Crear `NPedidos.java` con métodos estáticos que reciban `List<String> parametros`.
   - Validar parámetros, llamar a la capa de datos, retornar un String con el resultado.
   - Para listados, retornar formato tabla ASCII con `|` como separador de columnas.
3. **CapaPresentacion:** Crear `PPedidos.java` con método `generarHtml(comando, resultado)`.
   - Usar `construirPlantillaBase()` para el HTML con estilos inline (los clientes de correo no soportan CSS externo).
4. **Controller:** Crear `PedidoController.java` en `controllers/` con:
   - `canHandle(String comando)` — retorna true si el comando pertenece a este recurso.
   - `handle(String comando, List<String> parametros)` — switch/case que enruta a NPedidos.
5. **Router:** En `MailVerificationThread.ejecutarComando()`, agregar:
   ```java
   if (PedidoController.canHandle(comando)) {
       return PedidoController.handle(comando, parametros);
   }
   ```
6. **Ayuda:** Actualizar `obtenerAyuda()` en MailVerificationThread con los nuevos comandos.

### Convenciones de naming
- **Clases Dato:** `D` + NombreTabla (DUsuarios, DPedidos, DEnvases)
- **Clases Negocio:** `N` + NombreRecurso (NUsuarios, NPedidos)
- **Clases Presentación:** `P` + NombreRecurso (PUsuarios, PPedidos)
- **Controllers:** NombreRecurso + `Controller` (UsuarioController, PedidoController)
- **Comandos:** `CU{N}_{NN}` para casos de uso formales (usar guion bajo `_`, NO guion medio `-`), alias cortos como `LISPER`, `REGROL` para atajos

### Respuestas HTML
Las respuestas se envían como **HTML completo con CSS inline** (los clientes de correo como Gmail ignoran CSS externo y `<style>` en `<head>`).
- El tema visual es **caramelo/chocolate** (colores HSL: `#61381c`, `#8b5a2b`, `#fdfaf7`).
- Las tablas usan badges para estados (`SÍ/NO`, roles).
- Se detecta automáticamente si el contenido es HTML o texto plano.

---

## 🔧 Compilación y Ejecución

```powershell
# Desde la raíz del proyecto TecnoEmailZUZU/

# Compilar
javac --release 8 -d bin -cp "lib/*" -sourcepath src src/Main.java

# Ejecutar
java -cp "bin;lib/*" Main
```

### Dependencias (en lib/)
- `postgresql-42.7.3.jar` — Driver JDBC para PostgreSQL
- `javax.mail-1.6.2.jar` — (Presente pero NO usado actualmente; se usa sockets TCP puros)

---

## ⚠️ Notas Importantes

1. **NO usar frameworks.** Este proyecto es Java puro (Plain Java 8+), sin Spring, sin Maven, sin Gradle.
2. **SMTP y POP3 son sockets TCP puros.** No se usa JavaMail API. Se construyen los comandos SMTP/POP3 manualmente.
3. **El campo `activo` en `usuarios` es obligatorio.** La capa de datos (DUsuarios) lo lee y escribe. Si falta en la BD, se rompe con `"Column activo not found"`.
4. **Los envases son reutilizables.** Van del negocio al cliente (préstamo) y vuelven (devolución). El stock_disponible se decrementa al prestar e incrementa al devolver.
5. **La autenticación por email está temporalmente desactivada** en MailVerificationThread (bloque comentado). Cualquier correo puede ejecutar comandos.
6. **Los correos se eliminan del servidor POP3 después de procesarse** (comando DELE).
7. **El `.env` NO se sube a git** (está en `.gitignore`). Usar `.env.example` como referencia.
