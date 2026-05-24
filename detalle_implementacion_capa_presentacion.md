# Informe de Implementación — Capa de Presentación (Grupo 16)

Este documento detalla la estructura y el desarrollo de la **Capa de Presentación** del proyecto **TecnoEmailZUZU**. Describe el funcionamiento del hilo de red por sockets puros TCP, la subcapa de enrutamiento por controladores, el analizador léxico (`Analex`), y las vistas HTML5 dinámicas que dotan al sistema de una interfaz gráfica de alta fidelidad directamente en la bandeja de entrada del usuario.

---

## 1. Arquitectura de Separación por Controladores (Controllers)

Para evitar que el hilo de escucha de correos crezca indefinidamente a medida que agregamos nuevos casos de uso o CRUDs, estructuramos una arquitectura basada en **Controladores por Recurso** en el paquete `CapaPresentacion.controllers`:

```
               [ Bandeja de Correo POP3 ]
                           │
                           ▼ (Socket TCP Puerto 110)
               [ MailVerificationThread ] <─── Extract ───> [ Analex Parser ]
                           │
                           ▼ (Despachar Comando)
               [ Resource Controller Router ]
               ┌───────────┼───────────┐
               ▼           ▼           ▼
      [UsuarioController] [RolController] [EnvaseController] ...
               │           │
               ▼           ▼
          [NUsuarios]   [NRoles] (Capa de Negocio)
               │           │
               ▼           ▼
          [PUsuarios]   [PRoles] (HTML Views)
               │           │
               └─────┬─────┘
                     ▼
             (HTML Renderizado)
                     │
                     ▼ (Socket TCP Puerto 25)
               [ Servidor SMTP ] ─── Enviar a ───> [ Correo del Cliente (HTML5) ]
```

### Clases de Enrutamiento
1. **[UsuarioController.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaPresentacion/controllers/UsuarioController.java):**
   * Encapsula los comandos del **Caso de Uso 1 (CU1) - Gestión de Usuarios** (`CU1-01` a `CU1-07`, `INSPER`, `LISPER`).
   * Delega las órdenes a [NUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaNegocio/NUsuarios.java) y redirige la respuesta a la vista [PUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaPresentacion/PUsuarios.java) para su conversión a HTML.
2. **[RolController.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaPresentacion/controllers/RolController.java):**
   * Administra el catálogo de roles administrativo (`REGROL`, `EDTROL`, `DELROL`, `LISROL`, `VERROL`).
   * Delega a [NRoles.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaNegocio/NRoles.java) y rendering en [PRoles.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaPresentacion/PRoles.java).

---

## 2. Hilo del Servidor de Correo (MailVerificationThread)

La clase [MailVerificationThread.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/utils/MailVerificationThread.java) ejecuta un ciclo asíncrono e infinito en segundo plano (cada 10 segundos) estableciendo conexiones TCP directas.

### A) Conexión POP3 (Puerto 110)
Se conecta mediante sockets de red crudos y consume las peticiones siguiendo el flujo estándar:
1. `USER` / `PASS`: Autenticación con las credenciales académicas.
2. `STAT` / `LIST`: Verificación de correos nuevos en bandeja de entrada.
3. `RETR [id]`: Descarga la estructura MIME cruda del mensaje.
4. `DELE [id]`: Una vez procesado y respondido con éxito, marca el correo para ser eliminado físicamente del servidor para evitar procesamientos duplicados.
5. `QUIT`: Confirma y consolida las eliminaciones.

### B) Enrutamiento y Despacho
En lugar de un interruptor `switch` redundante, delega dinámicamente a los controladores mediante bloques condicionales ágiles y autogestionados:
```java
// 1. Recurso: Usuarios
if (UsuarioController.canHandle(comando)) {
    return UsuarioController.handle(comando, parametros);
}
// 2. Recurso: Roles
if (RolController.canHandle(comando)) {
    return RolController.handle(comando, parametros);
}
```

### C) Transmisión SMTP Dinámica (Puerto 25)
Utiliza la cabecera `MIME-Version: 1.0` y evalúa el contenido saliente para decidir de forma automática el protocolo de renderizado en el cliente de correo del destinatario:
* **HTML (MIME `text/html`):** Si el contenido comienza con `<!DOCTYPE html>` o `<html>`, activa la interpretación del motor HTML5 permitiendo layouts premium, tablas fluidas, tipografías personalizadas y sombras.
* **Plain Text (MIME `text/plain`):** Si es un comando simple (ej. `HELP`), se envía como texto plano respetando saltos de línea para garantizar máxima velocidad y compatibilidad.

---

## 3. El Analizador Léxico (Analex)

La clase [Analex.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/utils/analex/Analex.java) se encarga del procesamiento sintáctico y léxico del asunto (Subject) del correo electrónico. 

Soporta dos gramáticas de forma simultánea:
1. **Formato Multicorchete:** `COMANDO [arg1] [arg2] [arg3]`
2. **Formato CSV entre Corchetes (Oficial del Docente):** `COMANDO["arg1","arg2",...]`

### Algoritmo de Extracción
* **`getComando(String subject)`:** Extrae toda la cadena de texto previa al primer corchete `[`.
* **`getParametros(String subject)`:** Localiza el bloque interno entre el primer `[` y el último `]`. Si el contenido no cuenta con delimitadores múltiples (`][` o `] [`), ejecuta un escaneo carácter por carácter de estilo CSV que respeta comas internas si se encuentran entre comillas (`"`), removiendo automáticamente espacios sobrantes y comillas externas de cada argumento para limpiar la entrada a la Capa de Negocio.

---

## 4. Vistas HTML5 Dinámicas (PUsuarios y PRoles)

Para no contaminar la **Capa de Negocio** (la cual sigue devolviendo respuestas rápidas en texto/ASCII plano y limpio), implementamos **Vistas** de la Capa de Presentación en [PUsuarios.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaPresentacion/PUsuarios.java) y [PRoles.java](file:///e:/semestre%201-2026/TECNO/primer-parcial/TecnoEmailZUZU/src/CapaPresentacion/PRoles.java):

### A) El Parser ASCII-to-HTML
Estas clases cuentan con un traductor interno que analiza los resultados de negocio:
* Si la respuesta contiene el delimitador `|`, procesa línea por línea ignorando guiones.
* Divide la fila por `|` y crea dinámicamente un elemento de tabla estilizado `<table>`.
* Realiza traducción de palabras clave en componentes HTML visuales:
  * `"SÍ"` / `"ACTIVO"` -> `<span class="badge badge-si">SÍ</span>` (Verde suave).
  * `"NO"` / `"INACTIVO"` -> `<span class="badge badge-no">NO</span>` (Gris).
  * Nombres de rol -> `<span class="badge badge-rol">Propietario</span>` (Caramelo / Dorado).

### B) Diseño Visual (Repostería ZUZU Premium)
Las vistas inyectan un diseño visual unificado enfocado en la repostería y panadería fina:
* **Colores de Marca:** Encabezado con degradado caramelo y chocolate (`linear-gradient(135deg, #61381c, #8b5a2b)`).
* **Contenedores Modernos:** Tarjetas con sombras HSL difuminadas y bordes suavizados de `20px` para una experiencia premium.
* **Alertas Inteligentes:** Bordes delgados y rellenos tenues en tonos verdes para éxitos y rojizos para errores, facilitando la lectura instantánea.
