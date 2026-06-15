package CapaPresentacion.controladores;

import CapaNegocio.NCartillas;
import CapaNegocio.NPedidos;
import CapaPresentacion.PCartillas;
import java.util.List;

public class CartillaControlador {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU4-01") || comando.equals("VERCAR") || comando.equals("VER_CARTILLA")
            || comando.equals("CU4-02") || comando.equals("CU4-06") || comando.equals("CANJEAR_PREMIO");
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
            case "CU4-02":
            case "CU4-06":
            case "CANJEAR_PREMIO":
                rawResult = NPedidos.canjearPremio(parametros);
                break;
            default:
                rawResult = "Error: Comando de cartilla no soportado.";
        }

        return PCartillas.generarHtml(comando, rawResult);
    }
}

