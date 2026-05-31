package CapaPresentacion;

public class PProductos {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Gestión de Ventas - Repostería ZUZU", comando, resultado);
    }
}
