package CapaPresentacion;

public class PReportes {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Reportes y Estadísticas - Repostería ZUZU", comando, resultado);
    }
}
