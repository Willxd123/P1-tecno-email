package CapaPresentacion;

public class PCartilla {
    public static String generarHtml(String comando, String resultado) {
        return PlantillaBase.generarHtml("Cartilla del Cliente - Repostería ZUZU", comando, resultado);
    }
}
