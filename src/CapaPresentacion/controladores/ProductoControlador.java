package CapaPresentacion.controladores;

import CapaNegocio.NProductos;
import CapaPresentacion.PProductos;
import java.util.List;

public class ProductoControlador {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU5-01") || comando.equals("REGISTRAR_PRODUCTO") ||
               comando.equals("CU5-02") || comando.equals("EDITAR_PRODUCTO") ||
               comando.equals("CU5-03") || comando.equals("DISPONIBILIDAD_PRODUCTO") || comando.equals("ACTIVAR_DESACTIVAR_PRODUCTO") ||
               comando.equals("CU5-04") || comando.equals("LISTAR_PRODUCTOS") || comando.equals("LISPROD") ||
               comando.equals("CU5-05") || comando.equals("COSTO_PRODUCCION") ||
               comando.equals("CU2-09") || comando.equals("REGISTRAR_RECETA") ||
               comando.equals("CU2-10") || comando.equals("AGREGAR_INSUMO_RECETA") || comando.equals("AGREGAR_RECETA_INSUMO") ||
               comando.equals("CU2-11") || comando.equals("VER_RECETA");
     }
 
     public static String handle(String comando, List<String> parametros) {
         if (comando == null) return "Error: Comando nulo.";
         String rawResult;
 
         switch (comando.toUpperCase().trim()) {
             case "CU5-01":
             case "REGISTRAR_PRODUCTO":
                 rawResult = NProductos.registrarProducto(parametros);
                 break;
 
             case "CU5-02":
             case "EDITAR_PRODUCTO":
                 rawResult = NProductos.editarProducto(parametros);
                 break;
 
             case "CU5-03":
             case "DISPONIBILIDAD_PRODUCTO":
             case "ACTIVAR_DESACTIVAR_PRODUCTO":
                 rawResult = NProductos.cambiarDisponibilidad(parametros);
                 break;
 
             case "CU5-04":
             case "LISTAR_PRODUCTOS":
             case "LISPROD":
                 rawResult = NProductos.listarProductos();
                 break;
 
             case "CU5-05":
             case "COSTO_PRODUCCION":
                 rawResult = NProductos.verCostoProduccion(parametros);
                 break;
 
             case "CU2-09":
             case "REGISTRAR_RECETA":
                 rawResult = NProductos.registrarReceta(parametros);
                 break;
 
             case "CU2-10":
             case "AGREGAR_INSUMO_RECETA":
             case "AGREGAR_RECETA_INSUMO":
                 rawResult = NProductos.agregarInsumoAReceta(parametros);
                 break;
 
             case "CU2-11":
             case "VER_RECETA":
                 rawResult = NProductos.verReceta(parametros);
                 break;

            default:
                rawResult = "Error: Comando de producto no soportado.";
        }

        return PProductos.generarHtml(comando, rawResult);
    }
}

