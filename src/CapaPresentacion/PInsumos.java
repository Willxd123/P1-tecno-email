package CapaPresentacion;

public class PInsumos {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Gestión de Insumos - Repostería ZUZU", comando, resultado);
    }
}
