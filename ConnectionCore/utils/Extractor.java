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

        public static Email getEmail(String plain_text){
            return new Email(getFrom(plain_text),getDecodedSubject(plain_text));
        }

        private static String getFrom(String plain_text){
            String search = "Return-Path: <";
            int index_begin = plain_text.indexOf(search) + search.length();
            int index_end = plain_text.indexOf(">");
            return plain_text.substring(index_begin, index_end);
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
            String search = "Subject: ";
            int i = plain_text.indexOf(search) + search.length();
            String end_string = "";
            if(plain_text.contains(GMAIL)){
                end_string = "To:";
            } else if(plain_text.contains(HOTMAIL)){
                end_string = "Thread-Topic";
            } else if(plain_text.contains(YAHOO)){
                end_string = "MIME-Version:";
            } else if (plain_text.contains(FICCT)) {
                end_string = "Date:";
            }
            int e = plain_text.indexOf(end_string, i);
            //System.out.print(plain_text.substring(i, e));
            return plain_text.substring(i, e);
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
            System.out.println(plain_text);
            String search = "Subject: ";
            int i = plain_text.indexOf(search) + search.length();
            String end_string = "";

            if (plain_text.contains(GMAIL)) {
                System.out.println("GMAIL");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "To:";
            } else if (plain_text.contains(HOTMAIL)) {
                System.out.println("HOTMAIL");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "Thread-Topic";
            } else if (plain_text.contains(YAHOO)) {
                System.out.println("YAHOO");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "MIME-Version:";
            } else if (plain_text.contains(FICCT)) {
                System.out.println("FICCT");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "Date:";
            } else if (plain_text.contains(ICLOUD)) {
                System.out.println("ICLOUD");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "Message-Id:";
            } else if (plain_text.contains(OUTLOOK)) {
                System.out.println("OUTLOOK");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "Thread-Topic";
            } else if (plain_text.contains(TECNOWEB) && !plain_text.contains(EDU)) {
                System.out.println("TECNOWEB");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "MIME-Version:";
            } else if (plain_text.contains(EDU)) {
                System.out.println("EDU");
                end_string = plain_text.contains(THUNDERBIRD) ? "Content-Type:" : "To:";
            } else {
                System.out.println("DEFAULT");
                return "help";
            }

            int e = plain_text.indexOf(end_string, i);

            // Verificar si el índice es válido
            if (e == -1 || i >= e) {
                System.out.println("El índice para el final del subject no es válido.");
                return "help";
            }


            // Obtener el asunto sin decodificar
            String subject = plain_text.substring(i, e);

            // Decodificar el asunto utilizando javax.mail.internet.MimeUtility
            try {
                return MimeUtility.decodeText(subject);
            } catch (UnsupportedEncodingException ex) {
                // Manejar cualquier excepción que pueda ocurrir al decodificar el asunto.
                return subject; // Devolver el asunto sin decodificar en caso de error.
            }
        }
    }