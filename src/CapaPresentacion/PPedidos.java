package CapaPresentacion;

public class PPedidos {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Gestión de Pedidos - Repostería ZUZU", comando, resultado);
    }
}
