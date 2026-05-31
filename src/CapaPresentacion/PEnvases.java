package CapaPresentacion;

public class PEnvases {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Gestión de Envases - Repostería ZUZU", comando, resultado);
    }
}
