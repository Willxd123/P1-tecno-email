package CapaPresentacion;

public class PAyuda {

    public static String generarHtml() {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "  <meta charset=\"utf-8\">\n" +
               "  <style>\n" +
               "    body {\n" +
               "      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\n" +
               "      background-color: #fdfaf7;\n" +
               "      color: #4a3e3d;\n" +
               "      margin: 0;\n" +
               "      padding: 0;\n" +
               "    }\n" +
               "    .container {\n" +
               "      max-width: 900px;\n" +
               "      margin: 40px auto;\n" +
               "      background-color: #ffffff;\n" +
               "      border-radius: 20px;\n" +
               "      overflow: hidden;\n" +
               "      box-shadow: 0 10px 40px rgba(97, 56, 28, 0.08);\n" +
               "      border: 1px solid #f0e6df;\n" +
               "    }\n" +
               "    .header {\n" +
               "      background: linear-gradient(135deg, #d97706, #b45309);\n" +
               "      padding: 35px 20px;\n" +
               "      text-align: center;\n" +
               "      color: #ffffff;\n" +
               "    }\n" +
               "    .header h1 {\n" +
               "      margin: 0;\n" +
               "      font-size: 28px;\n" +
               "      font-weight: 700;\n" +
               "      letter-spacing: 0.8px;\n" +
               "      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);\n" +
               "    }\n" +
               "    .header p {\n" +
               "      margin: 8px 0 0 0;\n" +
               "      font-size: 14px;\n" +
               "      opacity: 0.9;\n" +
               "    }\n" +
               "    .content {\n" +
               "      padding: 35px 30px;\n" +
               "    }\n" +
               "    .module-title {\n" +
               "      font-size: 20px;\n" +
               "      font-weight: 600;\n" +
               "      margin-top: 30px;\n" +
               "      margin-bottom: 15px;\n" +
               "      color: #b45309;\n" +
               "      border-bottom: 2px solid #fbe5d6;\n" +
               "      padding-bottom: 8px;\n" +
               "    }\n" +
               "    .module-title:first-of-type {\n" +
               "      margin-top: 0;\n" +
               "    }\n" +
               "    table {\n" +
               "      width: 100%;\n" +
               "      border-collapse: separate;\n" +
               "      border-spacing: 0;\n" +
               "      margin-bottom: 30px;\n" +
               "      border-radius: 12px;\n" +
               "      overflow: hidden;\n" +
               "      border: 1px solid #eedfd4;\n" +
               "    }\n" +
               "    th {\n" +
               "      background-color: #fbe5d6;\n" +
               "      color: #78350f;\n" +
               "      font-weight: 600;\n" +
               "      text-align: left;\n" +
               "      padding: 12px 14px;\n" +
               "      font-size: 13px;\n" +
               "    }\n" +
               "    td {\n" +
               "      padding: 12px 14px;\n" +
               "      border-bottom: 1px solid #f7efe9;\n" +
               "      font-size: 13px;\n" +
               "      color: #4a3e3d;\n" +
               "    }\n" +
               "    tr:last-child td {\n" +
               "      border-bottom: none;\n" +
               "    }\n" +
               "    tr:nth-child(even) {\n" +
               "      background-color: #fdfbf9;\n" +
               "    }\n" +
               "    .code-pattern {\n" +
               "      font-family: 'Courier New', Courier, monospace;\n" +
               "      background-color: #fef3c7;\n" +
               "      padding: 3px 6px;\n" +
               "      border-radius: 4px;\n" +
               "      color: #78350f;\n" +
               "      font-size: 12px;\n" +
               "      display: inline-block;\n" +
               "      margin-bottom: 4px;\n" +
               "    }\n" +
               "    .code-example {\n" +
               "      font-family: 'Courier New', Courier, monospace;\n" +
               "      background-color: #f3f4f6;\n" +
               "      padding: 3px 6px;\n" +
               "      border-radius: 4px;\n" +
               "      color: #1f2937;\n" +
               "      font-size: 12px;\n" +
               "      display: inline-block;\n" +
               "    }\n" +
               "    .footer {\n" +
               "      background-color: #fcf8f5;\n" +
               "      padding: 25px;\n" +
               "      text-align: center;\n" +
               "      font-size: 12px;\n" +
               "      color: #8c7b70;\n" +
               "      border-top: 1px solid #f0e6df;\n" +
               "    }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"container\">\n" +
               "    <div class=\"header\">\n" +
               "      <img src=\"https://i.ibb.co/RpQ8WGhK/bienvenida.png\" alt=\"Chifones Peruanos Zuzú Logo\" style=\"max-height: 80px; margin-bottom: 12px; display: block; margin-left: auto; margin-right: auto;\">\n" +
               "      <h1>SISTEMA DE “CHIFONES PERUANOS ZUZÚ” - GUÍA DE COMANDOS</h1>\n" +
               "      <p>Manual de Sintaxis de Casos de Uso y Operaciones vía Correo</p>\n" +
               "    </div>" +
               "    <div class=\"content\">\n" +
               "      <div class=\"module-title\">Gestión de Usuarios</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_USUARIO</strong></td>\n" +
               "          <td>Registrar Usuario (Create)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_USUARIO[\"Nombre\",\"Apellido\",\"Telefono\",\"Email\",\"Password\",\"Rol\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_USUARIO[\"Juan\",\"Perez\",\"77712345\",\"juan@gmail.com\",\"hash123\",\"cliente\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>EDITAR_USUARIO</strong></td>\n" +
               "          <td>Editar Usuario (Update)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">EDITAR_USUARIO[\"ID\",\"Nombre\",\"Apellido\",\"Telefono\",\"Email\"]</span><br>\n" +
               "            <span class=\"code-example\">EDITAR_USUARIO[\"3\",\"Juan Carlos\",\"Perez\",\"77712345\",\"juan.carlos@gmail.com\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>CAMBIAR_PASSWORD</strong></td>\n" +
               "          <td>Cambiar Contraseña</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">CAMBIAR_PASSWORD[\"ID\",\"NuevaPassword\"]</span><br>\n" +
               "            <span class=\"code-example\">CAMBIAR_PASSWORD[\"3\",\"claveSegura789\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>DESACTIVAR_USUARIO</strong></td>\n" +
               "          <td>Desactivar Usuario (Delete)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">DESACTIVAR_USUARIO[\"ID\"]</span><br>\n" +
               "            <span class=\"code-example\">DESACTIVAR_USUARIO[\"3\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_USUARIOS</strong></td>\n" +
               "          <td>Listar Usuarios (Read)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_USUARIOS</span> o <span class=\"code-pattern\">CU1-05</span> o <span class=\"code-pattern\">LISUSR</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>BUSCAR_USUARIO</strong></td>\n" +
               "          <td>Buscar Usuario (Filtro)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">BUSCAR_USUARIO[\"TextoABuscar\"]</span><br>\n" +
               "            <span class=\"code-example\">BUSCAR_USUARIO[\"Carlos\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>VER_PERFIL</strong></td>\n" +
               "          <td>Ver Perfil Propio</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">VER_PERFIL[\"ID\"]</span><br>\n" +
               "            <span class=\"code-example\">VER_PERFIL[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_ROL</strong></td>\n" +
               "          <td>Registrar Rol (Auxiliar)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_ROL[\"NombreRol\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_ROL[\"secretario\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>EDITAR_ROL</strong></td>\n" +
               "          <td>Editar Rol (Auxiliar)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">EDITAR_ROL[\"ID\",\"NuevoNombre\"]</span><br>\n" +
               "            <span class=\"code-example\">EDITAR_ROL[\"3\",\"secretario_premium\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>ELIMINAR_ROL</strong></td>\n" +
               "          <td>Eliminar Rol (Auxiliar)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">ELIMINAR_ROL[\"ID\"]</span><br>\n" +
               "            <span class=\"code-example\">ELIMINAR_ROL[\"3\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_ROLES</strong></td>\n" +
               "          <td>Listar Roles (Auxiliar)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_ROLES</span> o <span class=\"code-pattern\">LISROL</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>VER_ROL</strong></td>\n" +
               "          <td>Ver Detalles de Rol (Auxiliar)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">VER_ROL[\"ID\"]</span><br>\n" +
               "            <span class=\"code-example\">VER_ROL[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- GESTION DE INSUMOS -->\n" +
               "      <div class=\"module-title\">Gestión de Insumos</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_INSUMO</strong></td>\n" +
               "          <td>Registrar Insumo (Create)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_INSUMO[\"Nombre\",\"UnidadMedida\",\"StockActual\",\"StockMinimo\",\"CostoUnitario\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_INSUMO[\"Harina de trigo\",\"g\",\"5000\",\"500\",\"0.008\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>EDITAR_INSUMO</strong></td>\n" +
               "          <td>Editar Insumo (Update)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">EDITAR_INSUMO[\"ID\",\"Nombre\",\"CostoUnitario\",\"StockMinimo\"]</span><br>\n" +
               "            <span class=\"code-example\">EDITAR_INSUMO[\"1\",\"Harina Fina\",\"0.010\",\"600\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_INSUMOS</strong></td>\n" +
               "          <td>Listar Insumos (Read)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_INSUMOS</span> o <span class=\"code-pattern\">LISINS</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>ENTRADA_INSUMO</strong></td>\n" +
               "          <td>Registrar Entrada de Stock</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">ENTRADA_INSUMO[\"ID\",\"Cantidad\",\"Descripcion\"]</span><br>\n" +
               "            <span class=\"code-example\">ENTRADA_INSUMO[\"1\",\"1000\",\"Compra de saco de 1kg\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>AJUSTE_INSUMO</strong></td>\n" +
               "          <td>Registrar Ajuste de Stock</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">AJUSTE_INSUMO[\"ID\",\"Cantidad\",\"Descripcion\"]</span><br>\n" +
               "            <span class=\"code-example\">AJUSTE_INSUMO[\"1\",\"50\",\"Correccion por pesaje\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>MERMA_INSUMO</strong></td>\n" +
               "          <td>Registrar Merma de Insumo</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">MERMA_INSUMO[\"ID\",\"Cantidad\",\"Descripcion\"]</span><br>\n" +
               "            <span class=\"code-example\">MERMA_INSUMO[\"1\",\"150\",\"Deterioro por humedad\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>HISTORIAL_INSUMO</strong></td>\n" +
               "          <td>Ver Historial de Movimientos</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">HISTORIAL_INSUMO[\"InsumoID\"]</span><br>\n" +
               "            <span class=\"code-example\">HISTORIAL_INSUMO[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>ALERTAS_INSUMO</strong></td>\n" +
               "          <td>Ver Alertas de Reposición</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">ALERTAS_INSUMO</span> o <span class=\"code-pattern\">ALEINS</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_RECETA</strong></td>\n" +
               "          <td>Registrar Receta</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_RECETA[\"ProductoID\",\"NombreReceta\",\"Descripcion\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_RECETA[\"1\",\"Receta Torta Chocolate\",\"Ingredientes basicos\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>AGREGAR_INSUMO_RECETA</strong></td>\n" +
               "          <td>Agregar Insumo a Receta</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">AGREGAR_INSUMO_RECETA[\"RecetaID\",\"InsumoID\",\"Cantidad\"]</span><br>\n" +
               "            <span class=\"code-example\">AGREGAR_INSUMO_RECETA[\"1\",\"1\",\"200.000\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>VER_RECETA</strong></td>\n" +
               "          <td>Ver Receta de un Producto</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">VER_RECETA[\"ProductoID\"]</span><br>\n" +
               "            <span class=\"code-example\">VER_RECETA[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- MÓDULO 3: ENVASES -->\n" +
               "      <div class=\"module-title\">Gestión de Envases</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_ENVASE</strong></td>\n" +
               "          <td>Registrar Tipo de Envase (Create)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_ENVASE[\"Nombre\",\"Descripcion\",\"StockTotal\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_ENVASE[\"Caja Mediana\",\"Caja de carton blanca\",\"50\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>EDITAR_ENVASE</strong></td>\n" +
               "          <td>Editar Tipo de Envase (Update)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">EDITAR_ENVASE[\"ID\",\"Nombre\",\"Descripcion\",\"StockTotal\"]</span><br>\n" +
               "            <span class=\"code-example\">EDITAR_ENVASE[\"1\",\"Caja Mediana Premium\",\"Caja reforzada\",\"60\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_ENVASES</strong></td>\n" +
               "          <td>Ver Stock de Envases (Read)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_ENVASES</span> o <span class=\"code-pattern\">LISENV</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>PRESTAMO_ENVASE</strong></td>\n" +
               "          <td>Registrar Préstamo de Envases</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">PRESTAMO_ENVASE[\"PedidoOrigenID\",\"EnvaseID\",\"CantidadPrestada\"]</span><br>\n" +
               "            <span class=\"code-example\">PRESTAMO_ENVASE[\"1\",\"1\",\"2\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>DEVOLUCION_ENVASE</strong></td>\n" +
               "          <td>Registrar Devolución de Envases</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">DEVOLUCION_ENVASE[\"PedidoOrigenID\",\"PedidoDevolucionID\",\"EnvaseID\",\"CantidadDevuelta\"]</span><br>\n" +
               "            <span class=\"code-example\">Ejemplo (1 envase): DEVOLUCION_ENVASE[\"1029\",\"1029\",\"2\",\"1\"]</span><br>\n" +
               "            <span class=\"code-example\">Ejemplo (más envases): DEVOLUCION_ENVASE[\"1024\",\"1026\",\"2\",\"3\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>ENVASES_PENDIENTES</strong></td>\n" +
               "          <td>Ver Envases Pendientes</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">ENVASES_PENDIENTES</span> o <span class=\"code-pattern\">CU3-06</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>HISTORIAL_ENVASES</strong></td>\n" +
               "          <td>Ver Historial de un Cliente</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">HISTORIAL_ENVASES[\"ClienteID\"]</span><br>\n" +
               "            <span class=\"code-example\">HISTORIAL_ENVASES[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- MÓDULO 4: CARTILLA -->\n" +
               "      <div class=\"module-title\">Gestión de Cartilla</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>VER_CARTILLA</strong></td>\n" +
               "          <td>Ver Cartilla del Cliente (Read)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">VER_CARTILLA[\"ClienteID\"]</span> o <span class=\"code-pattern\">VERCAR[\"ClienteID\"]</span><br>\n" +
               "            <span class=\"code-example\">VER_CARTILLA[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>CANJEAR_PREMIO</strong></td>\n" +
               "          <td>Canjear Chifón de Regalo (Premio)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">CANJEAR_PREMIO[\"ClienteID\", \"ProductoID\"]</span><br>\n" +
               "            <span class=\"code-example\">CANJEAR_PREMIO[\"1\", \"11\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- MÓDULO 5: VENTAS / PRODUCTOS -->\n" +
               "      <div class=\"module-title\">Gestión de Productos</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_PRODUCTO</strong></td>\n" +
               "          <td>Registrar Producto (Create)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_PRODUCTO[\"Nombre\",\"Descripcion\",\"PrecioUnitario\",\"CategoriaID*\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_PRODUCTO[\"Torta de Vainilla\",\"Torta con chispas\",\"80.00\",\"1\"]</span><br>\n" +
               "            <small style=\"color: #8c7b70;\">* CategoriaID es opcional. Usar 0 o null para omitir.</small>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>EDITAR_PRODUCTO</strong></td>\n" +
               "          <td>Editar Producto (Update)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">EDITAR_PRODUCTO[\"ID\",\"Nombre\",\"Descripcion\",\"PrecioUnitario\",\"CategoriaID*\"]</span><br>\n" +
               "            <span class=\"code-example\">EDITAR_PRODUCTO[\"2\",\"Torta de Vainilla Fina\",\"Torta esponjosa\",\"90.00\",\"1\"]</span><br>\n" +
               "            <small style=\"color: #8c7b70;\">* CategoriaID es opcional. Usar 0 o null para omitir.</small>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>DISPONIBILIDAD_PRODUCTO</strong></td>\n" +
               "          <td>Activar/Desactivar Producto</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">DISPONIBILIDAD_PRODUCTO[\"ID\",\"Disponible\"]</span><br>\n" +
               "            <span class=\"code-example\">DISPONIBILIDAD_PRODUCTO[\"2\",\"false\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_PRODUCTOS</strong></td>\n" +
               "          <td>Listar Productos (Read)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_PRODUCTOS</span> o <span class=\"code-pattern\">LISPROD</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>COSTO_PRODUCCION</strong></td>\n" +
               "          <td>Ver Costo de Producción</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">COSTO_PRODUCCION[\"ID\"]</span><br>\n" +
               "            <span class=\"code-example\">COSTO_PRODUCCION[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REGISTRAR_CATEGORIA</strong></td>\n" +
               "          <td>Registrar Categoría</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REGISTRAR_CATEGORIA[\"Nombre\"]</span><br>\n" +
               "            <span class=\"code-example\">REGISTRAR_CATEGORIA[\"Chifones Familiares\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_CATEGORIAS</strong></td>\n" +
               "          <td>Listar Categorías</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_CATEGORIAS</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>ASIGNAR_CATEGORIA</strong></td>\n" +
               "          <td>Asignar Categoría a Producto</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">ASIGNAR_CATEGORIA[\"ProductoID\",\"CategoriaID\"]</span><br>\n" +
               "            <span class=\"code-example\">ASIGNAR_CATEGORIA[\"12\",\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>QUITAR_CATEGORIA</strong></td>\n" +
               "          <td>Quitar Categoría de Producto</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">QUITAR_CATEGORIA[\"ProductoID\"]</span><br>\n" +
               "            <span class=\"code-example\">QUITAR_CATEGORIA[\"12\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- MÓDULO 6: PEDIDOS -->\n" +
               "      <div class=\"module-title\">Gestión de Pedidos</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>CREAR_PEDIDO</strong></td>\n" +
               "          <td>Crear Pedido (Create)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">CREAR_PEDIDO[\"ClienteID\",\"ProductoID\",\"Cantidad\",\"TipoPago\",\"CantCuotas\"]</span><br>\n" +
               "            <span class=\"code-example\">Ejemplo al Contado: CREAR_PEDIDO[\"1\",\"1\",\"2\",\"contado\",\"0\"]</span><br>\n" +
               "            <span class=\"code-example\">Ejemplo en Cuotas: CREAR_PEDIDO[\"1\",\"1\",\"2\",\"cuotas\",\"3\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>DETALLE_PEDIDO</strong></td>\n" +
               "          <td>Ver Detalle de Pedido (Read)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">DETALLE_PEDIDO[\"PedidoID\"]</span><br>\n" +
               "            <span class=\"code-example\">DETALLE_PEDIDO[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>LISTAR_PEDIDOS</strong></td>\n" +
               "          <td>Listar Pedidos (Read List)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">LISTAR_PEDIDOS</span> o <span class=\"code-pattern\">LISPED</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>CAMBIAR_ESTADO_PEDIDO</strong></td>\n" +
               "          <td>Cambiar Estado de Pedido (Update)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">CAMBIAR_ESTADO_PEDIDO[\"PedidoID\",\"Estado\"]</span><br>\n" +
               "            <span class=\"code-example\">CAMBIAR_ESTADO_PEDIDO[\"1\",\"entregado\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>CANCELAR_PEDIDO</strong></td>\n" +
               "          <td>Cancelar Pedido (Delete)</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">CANCELAR_PEDIDO[\"PedidoID\"]</span><br>\n" +
               "            <span class=\"code-example\">CANCELAR_PEDIDO[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- MÓDULO 7: PAGOS -->\n" +
               "      <div class=\"module-title\">Gestión de Pagos</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>PAGAR_CUOTA</strong></td>\n" +
               "          <td>Registrar Pago de Cuota</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">PAGAR_CUOTA[\"CuotaID\"]</span><br>\n" +
               "            <span class=\"code-example\">PAGAR_CUOTA[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>ESTADO_CUOTAS</strong></td>\n" +
               "          <td>Ver Estado de Cuotas</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">ESTADO_CUOTAS[\"PedidoID\"]</span><br>\n" +
               "            <span class=\"code-example\">ESTADO_CUOTAS[\"1\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>CUOTAS_VENCIDAS</strong></td>\n" +
               "          <td>Ver Cuotas Vencidas</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">CUOTAS_VENCIDAS</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "\n" +
               "      <!-- MÓDULO 8: REPORTES -->\n" +
               "      <div class=\"module-title\">Reportes Estadísticos</div>\n" +
               "      <table>\n" +
               "        <tr>\n" +
               "          <th style=\"width: 25%\">Comando / Caso de Uso</th>\n" +
               "          <th style=\"width: 25%\">Operación / Acción</th>\n" +
               "          <th style=\"width: 50%\">Formato & Ejemplo de Asunto</th>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REPORTE_VENTAS</strong></td>\n" +
               "          <td>Reporte de Ventas por Período</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REPORTE_VENTAS[\"FechaInicio\",\"FechaFin\"]</span><br>\n" +
               "            <span class=\"code-example\">REPORTE_VENTAS[\"2026-05-01\",\"2026-05-31\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REPORTE_INGRESOS</strong></td>\n" +
               "          <td>Reporte Ingresos Contado vs Crédito</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REPORTE_INGRESOS[\"FechaInicio\",\"FechaFin\"]</span><br>\n" +
               "            <span class=\"code-example\">REPORTE_INGRESOS[\"2026-05-01\",\"2026-05-31\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REPORTE_CUOTAS_PENDIENTES</strong></td>\n" +
               "          <td>Reporte de Cuotas Pendientes</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REPORTE_CUOTAS_PENDIENTES</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REPORTE_CONSUMO_INSUMOS</strong></td>\n" +
               "          <td>Reporte de Consumo de Insumos</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REPORTE_CONSUMO_INSUMOS[\"FechaInicio\",\"FechaFin\"]</span><br>\n" +
               "            <span class=\"code-example\">REPORTE_CONSUMO_INSUMOS[\"2026-05-01\",\"2026-05-31\"]</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REPORTE_STOCK_CRITICO</strong></td>\n" +
               "          <td>Reporte de Stock Crítico de Insumos</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REPORTE_STOCK_CRITICO</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "        <tr>\n" +
               "          <td><strong>REPORTE_ENVASES_PRESTADOS</strong></td>\n" +
               "          <td>Reporte de Envases Prestados</td>\n" +
               "          <td>\n" +
               "            <span class=\"code-pattern\">REPORTE_ENVASES_PRESTADOS</span>\n" +
               "          </td>\n" +
               "        </tr>\n" +
               "      </table>\n" +
               "      \n" +
               "    </div>\n" +
               "    <div class=\"footer\">\n" +
               "      <strong>Grupo 16 - Tecnología Web (UAGRM)</strong><br>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }
}
