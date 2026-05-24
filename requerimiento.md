INGENIERÍA INFORMÁTICA
TECNOLOGÍA WEB
PROYECTOS GESTIÓN


Felicitaciones, usted ha sido contratado como consultor para diseñar y desarrollar un sistema vía E-Mail
 
El sistema a desarrollar es via correo electrónico, esto quiere decir que el cliente realizara solicitudes mediante el uso del correo electrónico, la solicitud ira en el Asunto del Correo y si se requieren algún parámetro también deben ir en el Asunto por ejemplo:
 
Ejemplo 1:
mail from : luirfer.camacho@gmail.com
rcpt to: grupo20sc@tecnoweb.org.bo
subject: LISPER["*"]
 
Este correo al ser leído desde grupo20sc@tecnoweg,org.bo deberá verificar si el comando existe y si los parámetros son los correctos, entonces deberá realizar un select de la tabla persona y con dicha información devolver un correo a luifer.camacho@gmail.com que  "Listando las Personas", caso contrario deberá informar a luifer.camacho@gmail.com el problema que hubo "Error" al intentar realizar la orden.
 
Ejemplo 2:
mail from : luirfer.camacho@gmail.com
rcpt to: grupo20sc@tecnoweb.org.bo
subject: INSPER["4715292","Juan Carlos","Perez Seras","Estudiante","33554433","71055123","juanperez@uagrm.edu.bo"]
 
Este correo al ser leído desde grupo20sc@tecnoweg,org.bo deberá verificar si el comando existe y si los parámetros son los correctos, entonces deberá Insertar a la tabla persona e informar luifer.camacho@gmail.com que la operación fue un "Éxito", caso contrario deberá informar a luifer.camacho@gmail.com el problema que hubo "Error" al intentar realizar la orden.
 
Espero logre concluir la implementación de cada uno de los caso de uso de su sistema asignado.

 
Nota:

La Creación y Administración de la Base de Datos se puede realizar desde cualquiera de los laboratorios de computación para ello se tiene una cuenta de usuario (similar a la cuenta de correo) en el gestor de base de datos POSTGRESQL dentro del servidor de la Facultad (mail.tecnoweb.org.bo).

user: grupo20sc

pass: grup020grup020*

DB : db_grupo20sc

Server : mail.tecnoweb.org.bo

 

Presentación:

Se deberá enviar dos documentos:

Documento con caratula de los integrantes (PUDS)
Proyecto  (Codigo Fuente )
El documento y el proyecto debe ser enviado en formato comprimido con el siguiente nombre: 2026-1_INF513-P1_grupoXXsc.tar.gz

Tambien debe ser subido Classroom GitHub (Obligatorio) : https://classroom.github.com/a/vNTX0PXp

El Documento debe ser presentado antes de la defensa en formato impreso.

Defensa:

Tiempo : 15-20 minutos.
Los Grupos en Orden Ascendente, se esperara un tiempo de 5 minutos por grupo en caso de no presentarse, se procederá a la siguiente grupo.
El documento comprimido debe ser enviado y entregado  en formato zip,
Estos trabajos se deben enviar antes de las 07:00  según la fecha definida.