    /*
     * To change this license header, choose License Headers in Project Properties.
     * To change this template file, choose Tools | Templates
     * and open the template in the editor.
     */
    package utils;

    import java.io.UnsupportedEncodingException;
    import javax.mail.internet.MimeUtility;

    public class Extractor {
        private static String GMAIL = "d=gmail";
        private static String HOTMAIL = "d=hotmail";
        private static String YAHOO = "d=yahoo";
        private static final String FICCT = "d=ficct";
        private static final String ICLOUD = "d=icloud";
        private static final String OUTLOOK = "d=outlook";
        private static final String TECNOWEB = "tecnoweb.org.bo";
        private static final String EDU = "uagrm.edu.bo";
        private static final String THUNDERBIRD = "Thunderbird";

        public static Correo getCorreo(String plain_text){
            return new Correo(getFrom(plain_text),getDecodedSubject(plain_text));
        }

        private static String getFrom(String plain_text){
            String search = "Return-Path: <";
            int index_begin = plain_text.indexOf(search);
            if (index_begin == -1) {
                // Fallback a "From: " si no hay Return-Path
                search = "From: ";
                index_begin = plain_text.indexOf(search);
                if (index_begin == -1) return "unknown@localhost";
                index_begin += search.length();
                int index_end = plain_text.indexOf("\n", index_begin);
                if (index_end == -1) index_end = plain_text.length();
                String fromLine = plain_text.substring(index_begin, index_end).trim();
                if (fromLine.contains("<") && fromLine.contains(">")) {
                    return fromLine.substring(fromLine.indexOf("<") + 1, fromLine.indexOf(">")).trim();
                }
                return fromLine;
            }
            index_begin += search.length();
            int index_end = plain_text.indexOf(">", index_begin);
            if (index_end == -1) return "unknown@localhost";
            return plain_text.substring(index_begin, index_end).trim();
        }


        private static String getTo(String plain_text){
            String to = "";
            if(plain_text.contains(GMAIL)){
                to = getToFromGmail(plain_text);
            } else if(plain_text.contains(HOTMAIL)){
                to = getToFromHotmail(plain_text);
            } else if(plain_text.contains(YAHOO)){
                to = getToFromYahoo(plain_text);
            } else if(plain_text.contains(FICCT)){
                to = getToFromFicct(plain_text);
            } else if(plain_text.contains(ICLOUD)){
                to = getToFromFicct(plain_text);
            } else if(plain_text.contains(OUTLOOK)){
                to = getToFromFicct(plain_text);
            } else if(plain_text.contains(TECNOWEB)){
                to = getToFromFicct(plain_text);
            } else if(plain_text.contains(EDU)){
                to = getToFromEDU(plain_text);
            }
            return to;
        }

        private static String getSubject(String plain_text){
            return getDecodedSubject(plain_text);
        }

        private static String getToFromGmail(String plain_text){
            return getToCommon(plain_text);
        }

        private static String getToFromEDU(String plain_text){
            return getToCommon(plain_text);
        }

        private static String getToFromFicct(String plain_text){
            String to_line = getToCommon(plain_text).trim();
            // Si tiene formato Nombre <correo@dominio>
            if (to_line.contains("<") && to_line.contains(">")) {
                int i = to_line.indexOf("<") + 1;
                int j = to_line.indexOf(">");
                return to_line.substring(i, j).trim();
            }
            return to_line;
        }


        private static String getToFromHotmail(String plain_text){
            String aux = getToCommon(plain_text);
            return aux.substring(1, aux.length() - 1);
        }

        private static String getToFromYahoo(String plain_text){
            int index = plain_text.indexOf("To: ");
            int i = plain_text.indexOf("<", index);
            int e = plain_text.indexOf(">", i);
            return plain_text.substring(i + 1, e);
        }

        private static String getToCommon(String plain_text){
            String aux = "To: ";
            int index_begin = plain_text.indexOf(aux) + aux.length();
            int index_end = plain_text.indexOf("\n", index_begin);
            return plain_text.substring(index_begin, index_end);
        }

        private static String getDecodedSubject(String plain_text) {
            String search = "Subject: ";
            int i = plain_text.indexOf(search);
            if (i == -1) {
                return "help";
            }
            i += search.length();
            int e = plain_text.indexOf("\n", i);
            if (e == -1) {
                e = plain_text.length();
            }

            // Obtener el asunto sin decodificar y quitar espacios/retornos
            String subject = plain_text.substring(i, e).trim();
            if (subject.endsWith("\r")) {
                subject = subject.substring(0, subject.length() - 1);
            }

            // Decodificar el asunto utilizando javax.mail.internet.MimeUtility
            try {
                return MimeUtility.decodeText(subject);
            } catch (UnsupportedEncodingException ex) {
                // Manejar cualquier excepción que pueda ocurrir al decodificar el asunto.
                return subject; // Devolver el asunto sin decodificar en caso de error.
            }
        }
    }