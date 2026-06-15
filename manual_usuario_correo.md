# Manual de Usuario — Gestión por Correo Electrónico (Grupo 16)

Este manual describe el formato de entrada, las reglas de sintaxis y los comandos soportados para la administración y control del sistema **“CHIFONES PERUANOS ZUZÚ”** a través de correo electrónico utilizando el asunto (Subject).

---

## 1. Reglas Generales de Sintaxis

El motor analizador soporta dos formatos de comandos de forma transparente. Elige el que te resulte más cómodo:

1. **Formato CSV entre Corchetes (Recomendado/Oficial):**
   * Sintaxis: `COMANDO["parametro1","parametro2",...]`
   * Ejemplo: `CU1-01["Juan","Perez","71234567","123456","Cliente"]`
2. **Formato Multicorchete:**
   * Sintaxis: `COMANDO [parametro1] [parametro2] [parametro3]`
   * Ejemplo: `CU1-01 [Juan] [Perez] [71234567] [123456] [Cliente]`

> [!IMPORTANT]
> * **Asunto (Subject):** El comando y sus parámetros deben ir estrictamente en el Asunto del correo. El cuerpo del correo es ignorado.
> * **Comillas y espacios:** El sistema limpia automáticamente comillas externas y espacios al inicio y final de cada parámetro.

---

## 2. Comandos para Gestión de Usuarios (Caso de Uso 1)

### A) Registrar Usuario (`CU1-01` o `REGISTRAR_USUARIO`)
Inserta un nuevo usuario al sistema con contraseña hasheada en SHA-256.
* **Parámetros:** `["Nombre", "Apellido", "Teléfono", "Contraseña", "Rol"]`
* **Roles válidos:** `Propietario`, `Secretaria`, `Cliente` (No distingue mayúsculas).
* **Ejemplo de Entrada (Asunto):**
  `CU1-01["Carlos","Gomez","76543210","carlos2026","Cliente"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Usuario Carlos Gomez registrado con ID: 15 y Rol: Cliente.

---

### B) Editar Usuario (`CU1-02` o `EDITAR_USUARIO`)
Actualiza el perfil de un usuario existente.
* **Parámetros:** `["ID", "Nombre", "Apellido", "Teléfono"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-02["15","Carlos Daniel","Gomez Seras","76543211"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Usuario con ID 15 modificado correctamente.

---

### C) Cambiar Contraseña (`CU1-03` o `CAMBIAR_PASSWORD`)
Actualiza las credenciales de un usuario.
* **Parámetros:** `["ID", "NuevaContraseña"]` (Mínimo 6 caracteres).
* **Ejemplo de Entrada (Asunto):**
  `CU1-03["15","nuevaClave99"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Contraseña actualizada correctamente para el ID: 15.

---

### D) Desactivar Usuario (`CU1-04` o `DESACTIVAR_USUARIO`)
Inhabilita el ingreso lógico del usuario en el sistema.
* **Parámetros:** `["ID"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-04["15"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Usuario con ID 15 ha sido desactivado del sistema.

---

### E) Listar Usuarios (`CU1-05` o `LISTAR_USUARIOS`)
Muestra la lista de todos los usuarios registrados en una tabla HTML5 con badges de estado y rol.
* **Parámetros:** Ninguno.
* **Ejemplo de Entrada (Asunto):**
  `CU1-05`
* **Ejemplo de Respuesta Visual (HTML):**
  Retorna una tabla visual con el siguiente formato:

  | ID | Nombre Completo | Teléfono | Rol | Activo |
  | :--- | :--- | :--- | :--- | :--- |
  | 1 | Administrador ZUZU | 71001122 | **PROPIETARIO** | **SÍ** |
  | 15 | Carlos Daniel Gomez Seras | 76543211 | **CLIENTE** | **NO** |

---

### F) Buscar Usuario (`CU1-06` o `BUSCAR_USUARIO`)
Busca usuarios cuyas columnas (Nombre, Apellido, Teléfono o ID) coincidan con el texto de búsqueda.
* **Parámetros:** `["TextoABuscar"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-06["Daniel"]`
* **Ejemplo de Respuesta Visual (HTML):**
  Muestra una tabla con los usuarios que coincidan con la búsqueda.

---

### G) Ver Perfil Propio (`CU1-07` o `VER_PERFIL`)
Muestra el detalle detallado de un perfil.
* **Parámetros:** `["ID"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-07["1"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **=== DETALLE DEL PERFIL ===**
  > ID: 1  
  > Nombre: Administrador ZUZU  
  > Teléfono: 71001122  
  > Rol: Propietario  
  > Estado: ACTIVO (SÍ)

---

## 3. Comandos para Gestión de Roles (Caso de Uso 1 / CRUD Auxiliar)

### A) Registrar Rol (`CU1-08`, `REGROL` o `REGISTRAR_ROL`)
* **Parámetros:** `["NombreRol"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-08["Repostero"]` o `REGROL["Repostero"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Rol 'Repostero' registrado con ID: 4.

---

### B) Editar Rol (`CU1-09`, `EDTROL` o `EDITAR_ROL`)
* **Parámetros:** `["ID", "NuevoNombre"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-09["4","Repostero Fino"]` o `EDTROL["4","Repostero Fino"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Rol modificado correctamente. ID: 4 | Nuevo Nombre: Repostero Fino.

---

### C) Eliminar Rol (`CU1-10`, `DELROL` o `ELIMINAR_ROL`)
* **Parámetros:** `["ID"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-10["4"]` o `DELROL["4"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **🎉 OPERACIÓN EXITOSA**
  > Éxito: Rol con ID 4 (Repostero Fino) eliminado correctamente.

---

### D) Listar Roles (`CU1-11`, `LISROL` o `LISTAR_ROLES`)
* **Parámetros:** Ninguno.
* **Ejemplo de Entrada (Asunto):**
  `CU1-11` o `LISROL`
* **Ejemplo de Respuesta Visual (HTML):**
  Retorna una tabla visual con el siguiente formato:

  | ID | Nombre del Rol |
  | :--- | :--- |
  | 1 | **PROPIETARIO** |
  | 2 | **SECRETARIA** |
  | 3 | **CLIENTE** |

---

### E) Ver Rol (`CU1-12`, `VERROL` o `VER_ROL`)
* **Parámetros:** `["ID"]`
* **Ejemplo de Entrada (Asunto):**
  `CU1-12["2"]` o `VERROL["2"]`
* **Ejemplo de Respuesta Visual (HTML):**
  > **=== DETALLE DEL ROL ===**  
  > ID: 2  
  > Nombre: Secretaria

---

## 4. Comando de Ayuda (`HELP` o `AYUDA`)

Si tienes dudas sobre la sintaxis o los comandos admitidos, puedes enviar un correo con el Asunto:
`HELP` o `AYUDA`

El sistema te responderá inmediatamente con un correo en formato HTML que contiene la guía de uso de comandos de forma interactiva y detallada.
