package CapaPresentacion;

public class PPagos {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Gestión de Pagos - Repostería ZUZU", comando, resultado);
    }
}
