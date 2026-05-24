# Informe de Implementación — Lógica de Negocio y Utilidades (Grupo 16)

Este documento detalla la configuración y el desarrollo de la **Capa de Negocio** y las utilidades para el procesamiento de correos electrónicos, específicamente enfocado en el **Caso de Uso 1 (CU1) — Gestión de Usuarios**. Todo el trabajo se ha integrado respetando estrictamente la arquitectura del sistema y la compatibilidad con Java 8.

---

## 1. Corrección y Configuración Arquitectónica

Para mantener un estándar correcto en el desarrollo en Java, se realizó un reordenamiento de los archivos de utilidades:
* Se movió la carpeta externa `utils/` al interior del directorio principal del código fuente: **`src/utils/`**. 
* Esto asegura que las clases `Command.java`, `Email.java` y `Extractor.java` pertenezcan correctamente al paquete de Java y puedan ser compiladas de forma centralizada sin causar problemas de `classpath`.

### Integración de JavaMail API
La clase original `Extractor.java` utilizaba `javax.mail.internet.MimeUtility` para decodificar los asuntos del correo. Dado que esta librería no viene de forma predeterminada en el JDK 8 estándar, se descargó e instaló en el proyecto:
* **Librería instalada**: `javax.mail-1.6.2.jar` (dentro de la carpeta `lib/`).
* **Estado**: El proyecto ahora compila de forma exitosa sin errores de dependencias de correo.

---

## 2. Desarrollo del Analizador Léxico (Analex) y Validadores

Para procesar de forma segura las órdenes enviadas al sistema por correo, se crearon los subpaquetes de análisis y validación dentro de `utils`, aislando las responsabilidades correctamente:

### A) `utils.analex.Analex`
Actúa como un **Analizador Léxico**. Toma el asunto (Subject) crudo de un correo electrónico y extrae los comandos y los parámetros. 
* **Regla de Formato**: Extrae los parámetros de forma segura soportando dos esquemas alternativos de forma transparente:
  1. **Corchetes Múltiples:** `COMANDO [param1] [param2] [param3] ...` (ej., `CU1-01 [Juan] [Perez] [71234567] [juan@uagrm.edu.bo]`)
  2. **Corchetes Únicos con Comas (Formato Oficial del Docente):** `COMANDO["param1","param2",...]` (ej., `INSPER["4715292","Juan Carlos","Perez Seras","Estudiante","33554433","71055123","juan@uagrm.edu.bo"]`)
* **Auto-limpieza:** Remueve automáticamente comillas y espacios adicionales de cada parámetro para evitar inconsistencias en las validaciones de negocio.

### B) `utils.validadores.UsuarioValidator`
Se encarga de ejecutar todas las reglas de negocio antes de intentar enviar datos a PostgreSQL:
* **Formatos de texto**: Evita registros nulos o vacíos.
* **Seguridad de contraseña**: Exige contraseñas de mínimo 6 caracteres.
* **Integridad del Teléfono**: Valida que los números de teléfono consistan entre 7 y 15 dígitos usando Expresiones Regulares (`\d{7,15}`).
* **Integridad del Rol**: Garantiza la asignación válida únicamente de los roles `Propietario`, `Secretaria` o `Cliente` (ignorando mayúsculas/minúsculas de manera inteligente).

---

## 3. Implementación de la Capa de Negocio (CU1)

### A) [NUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaNegocio/NUsuarios.java)
Se ha creado la clase central [NUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaNegocio/NUsuarios.java) que expone los siguientes métodos mapeados exactamente a tu documento `casos_de_uso.md`. Adicionalmente, cuenta con un utilitario privado interno de encriptación **SHA-256** para las contraseñas:

| Caso de Uso | Método en `NUsuarios` | Lógica Implementada |
| :--- | :--- | :--- |
| **CU1-01** (Registrar) | `registrarUsuario(List<String> args)` | Valida formato, comprueba que el rol exista en la base de datos, hashea la contraseña en SHA-256, y guarda el usuario. |
| **CU1-02** (Editar) | `editarUsuario(List<String> args)` | Busca el perfil por ID y actualiza su nombre, apellido y teléfono. |
| **CU1-03** (Cambiar Pass) | `cambiarPassword(List<String> args)` | Valida longitud, genera un nuevo hash SHA-256 y actualiza el campo del registro indicado por ID. |
| **CU1-04** (Desactivar) | `desactivarUsuario(List<String> args)` | Localiza al usuario por ID y lo inhabilita lógicamente. (*Nota: Para esto, se agregó un campo `activo` a la tabla y código de `DUsuarios`*). |
| **CU1-05** (Listar) | `listarUsuarios(List<String> args)` | Retorna un reporte en formato texto simulando una tabla ASCII que incluye el rol real traducido desde `DRoles`. |
| **CU1-06** (Buscar) | `buscarUsuario(List<String> args)` | Algoritmo iterativo que evalúa un string sin importar la capitalización. Muestra resultados por coincidencia en Nombre, Apellido, o Teléfono. |
| **CU1-07** (Ver Perfil) | `verPerfil(List<String> args)` | Consulta todos los detalles, estado y roles de un usuario específico basado en su ID. |

### B) [NRoles.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaNegocio/NRoles.java) (NUEVO)
Para ofrecer un control administrativo completo e integral sobre los privilegios del sistema, implementamos la clase de negocio `NRoles.java`. Esta clase actúa como puente directo con `DRoles` de la Capa de Datos, exponiendo una interfaz CRUD de texto plano para correo electrónico:

| Operación | Método en `NRoles` | Asunto del Correo | Función |
| :--- | :--- | :--- | :--- |
| **Registrar** | `registrarRol(List<String> args)` | `REGROL["NombreRol"]` | Añade un rol validando que no esté duplicado. |
| **Editar** | `editarRol(List<String> args)` | `EDTROL["ID", "NuevoNombre"]` | Modifica el nombre del rol por su identificador. |
| **Eliminar** | `eliminarRol(List<String> args)` | `DELROL["ID"]` | Elimina el rol del catálogo. |
| **Listar** | `listarRoles(List<String> args)` | `LISROL` | Genera un listado tabular ASCII de todos los roles. |
| **Ver** | `verRol(List<String> args)` | `VERROL["ID"]` | Retorna los detalles de un rol específico. |

---

## 4. Estado de la Arquitectura Actual
Todo el ecosistema de dependencias se ha recompilado tras implementar el Caso de Uso 1, retornando `cero errores` en compilación pura:

```powershell
javac --release 8 -d bin -cp "lib/postgresql-42.7.3.jar;lib/javax.mail-1.6.2.jar" -sourcepath src src/Main.java src/CapaDatos/*.java src/CapaDatos/enums/*.java src/configuracion/*.java src/utils/*.java src/utils/analex/*.java src/utils/validadores/*.java src/CapaNegocio/*.java
```

El proyecto se encuentra totalmente estabilizado, y listo para integrar el patrón Command y enviar la respuesta textual hacia tu Capa de Presentación (Bandeja de Correo).

---

## 5. Capa de Presentación y Procesador de Correos (100% Funcional)

Para materializar la comunicación cliente-servidor a través del protocolo de correo electrónico utilizando protocolos puros, se implementó:

### A) [MailVerificationThread.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/utils/MailVerificationThread.java)
Un hilo de ejecución en segundo plano (Daemon Thread) que se ejecuta continuamente en bucle cada 10 segundos realizando lo siguiente:
1. **Lectura POP3 (puerto 110):** Se conecta a través de un `Socket` TCP puro al servidor POP3. Realiza la autenticación mediante `USER` y `PASS` y lee la cantidad de correos con `STAT`.
2. **Descarga de mensajes (`RETR`):** Para cada correo nuevo, descarga el texto plano de la cabecera y el cuerpo hasta encontrar el punto final (`.`).
3. **Análisis de Órdenes:** Ejecuta el analizador léxico [Analex.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/utils/analex/Analex.java) sobre el asunto (Subject) del correo para extraer el comando y sus argumentos.
4. **Ejecución de Lógica:** Enruta y ejecuta las acciones sobre la Capa de Negocio ([NUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaNegocio/NUsuarios.java)), obteniendo el resultado textual exacto.
5. **Respuesta SMTP (puerto 25):** Abre un `Socket` TCP puro al servidor SMTP y transfiere la respuesta al correo remitente (`Return-Path`) simulando los comandos del protocolo (`HELO`, `MAIL FROM`, `RCPT TO`, `DATA`, `.`, `QUIT`).
6. **Eliminación en Servidor (`DELE`):** Si el mensaje fue procesado y respondido de forma correcta, lo marca para borrar y lo confirma con `QUIT` en POP3.

El hilo se activa automáticamente desde el punto de entrada [Main.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/Main.java) al levantar la aplicación.

