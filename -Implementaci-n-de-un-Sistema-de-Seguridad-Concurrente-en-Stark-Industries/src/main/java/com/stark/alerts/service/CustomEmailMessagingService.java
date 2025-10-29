package com.stark.alerts.service;

import com.stark.alerts.dto.AlertMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

/**
 * Servicio de email con destinatarios personalizados
 */
@Service
public class CustomEmailMessagingService {
    
    private final JavaMailSender mailSender;
    private final EmailConfigurationService emailConfigService;
    private final boolean htmlEnabled;
    
    public CustomEmailMessagingService(JavaMailSender mailSender,
                                       EmailConfigurationService emailConfigService,
                                       @Value("${app.alerts.email.html-enabled:true}") boolean htmlEnabled) {
        this.mailSender = mailSender;
        this.emailConfigService = emailConfigService;
        this.htmlEnabled = htmlEnabled;
    }
    
    /**
     * Envía email a destinatarios específicos
     */
    public boolean sendAlertToRecipients(AlertMessage message, List<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return false;
        }
        
        try {
            String emailFrom = emailConfigService.getEmailFrom();
            
            if (htmlEnabled) {
                return sendHtmlEmailToRecipients(message, recipients, emailFrom);
            } else {
                return sendPlainTextEmailToRecipients(message, recipients, emailFrom);
            }
        } catch (Exception e) {
            System.err.println("Error sending email alert to custom recipients: " + e.getMessage());
            return false;
        }
    }
    
    private boolean sendHtmlEmailToRecipients(AlertMessage message, List<String> recipients, String emailFrom) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setTo(recipients.toArray(new String[0]));
        helper.setFrom(emailFrom);
        helper.setSubject(buildSubject(message));
        helper.setText(buildHtmlContent(message), true);
        
        mailSender.send(mimeMessage);
        return true;
    }
    
    private boolean sendPlainTextEmailToRecipients(AlertMessage message, List<String> recipients, String emailFrom) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipients.toArray(new String[0]));
        mail.setFrom(emailFrom);
        mail.setSubject(buildSubject(message));
        mail.setText(buildPlainTextContent(message));
        
        mailSender.send(mail);
        return true;
    }
    
    private String buildSubject(AlertMessage message) {
        return String.format("[ALERTA %s] %s - %s", 
                message.getSeverity(), 
                message.getSensorName(), 
                message.getTimestamp().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
    
    private String buildHtmlContent(AlertMessage message) {
        String severityColor = getSeverityColor(message.getSeverity().name());
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Alerta de Seguridad - Stark Industries</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { background-color: %s; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { padding: 20px; }
                    .severity { font-size: 18px; font-weight: bold; }
                    .sensor { color: #666; margin: 10px 0; }
                    .message { background-color: #f9f9f9; padding: 15px; border-left: 4px solid %s; margin: 15px 0; }
                    .footer { background-color: #333; color: white; padding: 15px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 Alerta de Seguridad</h1>
                        <div class="severity">%s</div>
                    </div>
                    <div class="content">
                        <div class="message">
                            <strong>Hay un evento de la gravedad "%s" que proviene del sensor "%s"</strong>
                        </div>
                        <div class="sensor"><strong>Tipo:</strong> %s</div>
                        <div class="sensor"><strong>Timestamp:</strong> %s</div>
                        <div class="message">
                            <strong>Mensaje:</strong><br>
                            %s
                        </div>
                    </div>
                    <div class="footer">
                        Stark Industries Security System<br>
                        Sistema de Seguridad Concurrente
                    </div>
                </div>
            </body>
            </html>
            """, 
            severityColor, severityColor,
            message.getSeverity(),
            message.getSeverity(),
            message.getSensorName(),
            message.getType(),
            message.getTimestamp().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            message.getMessage()
        );
    }
    
    private String buildPlainTextContent(AlertMessage message) {
        return String.format("""
            ALERTA DE SEGURIDAD - STARK INDUSTRIES
            
            Hay un evento de la gravedad "%s" que proviene del sensor "%s"
            
            Tipo: %s
            Timestamp: %s
            
            Mensaje:
            %s
            
            ---
            Stark Industries Security System
            """, 
            message.getSeverity(),
            message.getSensorName(),
            message.getType(),
            message.getTimestamp().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            message.getMessage()
        );
    }
    
    private String getSeverityColor(String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> "#dc3545"; // Rojo
            case "WARN" -> "#ffc107";     // Amarillo
            case "INFO" -> "#17a2b8";     // Azul
            default -> "#6c757d";         // Gris
        };
    }
}



