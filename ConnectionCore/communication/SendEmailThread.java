/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package communication;

import utils.Email;
//import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.activation.DataHandler;
//import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
//import javax.mail.util.ByteArrayDataSource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 *
 * @author teito
 */
public class SendEmailThread implements Runnable {

    //Descomentar para GMAIL
    private final static String PORT_SMTP = loadEnvVar("SMTP_PORT");
    private final static String PROTOCOL = loadEnvVar("SMTP_PROTOCOL");
    private final static String HOST = loadEnvVar("SMTP_HOST");
    private final static String USER = loadEnvVar("SMTP_USER");
    private final static String MAIL = loadEnvVar("SMTP_MAIL");
    private final static String MAIL_PASSWORD = loadEnvVar("SMTP_PASSWORD");

    private static String loadEnvVar(String name) {
        Dotenv dotenv = Dotenv.configure().load();
        return dotenv.get(name);
    }


    //Descomentar para tecnoweb
    /*
    private final static String PORT_SMTP = "25";
    private final static String PROTOCOL = "smtp";
    private final static String HOST = "mail.tecnoweb.org.bo";
    private final static String USER = "grupo09sc";
    private final static String PASSWORD = "grup009grup009";
    private final static String MAIL = "grupo09sc@tecnoweb.org.bo";
    private final static String MAIL_PASSWORD = "grupo09grupo09";

     */

    private final Email email;

    public SendEmailThread(Email email) {
        this.email = email;
    }

    @Override
    public void run() {

        //Descomentar para gmail
/*
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", HOST);
        properties.setProperty("mail.smtp.port", PORT_SMTP);
        //properties.setProperty("mail.smtp.starttls.enable", "false");//cuando user tecnoweb
        properties.setProperty("mail.smtp.ssl.enable", "true");//cuando usen Gmail
        properties.setProperty("mail.smtp.auth", "true");
*/
        //Descomentar para tecnoweb

        Properties properties = new Properties();
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.port", PORT_SMTP);
        properties.put("mail.smtp.auth", "false");
        properties.put("mail.smtp.starttls.enable", "false");
        

        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, MAIL_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MAIL));

            InternetAddress[] toAddresses = {new InternetAddress(email.getTo())};

            message.setRecipients(MimeMessage.RecipientType.TO, toAddresses);
            message.setSubject(email.getSubject());

            Multipart multipart = new MimeMultipart(); // No es necesario "related" en este caso
            MimeBodyPart htmlPart = new MimeBodyPart();

            // Agregar el contenido HTML o de texto
            htmlPart.setContent(email.getMessage(), "text/html; charset=utf-8");

            multipart.addBodyPart(htmlPart);

        /*    // Si se ha proporcionado una representación Base64 de la imagen, adjuntarla
            if (email.getImageFilePath() != null) {
                byte[] imageBytes = Base64.getDecoder().decode(email.getImageFilePath());
                DataSource imageDataSource = new ByteArrayDataSource(imageBytes, "image/png");
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.setDataHandler(new DataHandler(imageDataSource));
                imagePart.setFileName("chart.png");
                multipart.addBodyPart(imagePart);
            } */

            message.setContent(multipart);
            message.saveChanges();

            Transport.send(message);
        } catch (MessagingException ex) {
            Logger.getLogger(SendEmailThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
