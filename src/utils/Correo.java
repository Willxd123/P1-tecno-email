/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author ronal
 */
public class Correo {
    public static final String SUBJECT = "SOE -  Respuesta";
    private String from;
    private String to;
    private String subject;
    private String message;
    
    public Correo(){
    }

    public Correo(String to, String subject, String message) {
        this.to = to;
        this.subject = subject;
        this.message = message;
    }

    public Correo(String from, String subject) {
        this.from = from;
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        System.out.println(subject);
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "[From: " + from + ", To: " + to + ", Subject: " + subject + ", Message: " + message + "]";
    }
}
