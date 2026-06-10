package CapaPresentacion.controladores;

import CapaNegocio.NCartillas;
import CapaPresentacion.PCartillas;
import java.util.List;

public class CartillaControlador {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU4-01") || comando.equals("VERCAR") || comando.equals("VER_CARTILLA");
    }

    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;

        switch (comando.toUpperCase().trim()) {
            case "CU4-01":
            case "VERCAR":
            case "VER_CARTILLA":
                rawResult = NCartillas.verCartillaCliente(parametros);
                break;
            default:
                rawResult = "Error: Comando de cartilla no soportado.";
        }

        return PCartillas.generarHtml(comando, rawResult);
    }
}

