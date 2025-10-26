package com.stark.alerts.service;

import com.stark.alerts.dto.AlertMessage;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servicio SMS con números de teléfono personalizados
 */
@Service
public class CustomSmsMessagingService {
    
    private final ExecutorService executorService;
    
    public CustomSmsMessagingService() {
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Envía SMS a números específicos
     */
    public boolean sendAlertToNumbers(AlertMessage message, List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return false;
        }
        
        try {
            String smsContent = buildSmsContent(message);
            
            // Enviar SMS a todos los números especificados de forma asíncrona
            CompletableFuture.runAsync(() -> {
                for (String phoneNumber : phoneNumbers) {
                    sendSmsToNumber(phoneNumber, smsContent);
                }
            }, executorService);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error sending SMS alert to custom numbers: " + e.getMessage());
            return false;
        }
    }
    
    private void sendSmsToNumber(String phoneNumber, String content) {
        try {
            // Simulación de envío de SMS
            // En producción, aquí se integraría con un proveedor real como:
            // - Twilio
            // - AWS SNS
            // - Azure Communication Services
            // - Google Cloud Messaging
            
            System.out.println("📱 SMS enviado a " + phoneNumber + ": " + content);
            
            // Simular delay de red
            Thread.sleep(100);
            
        } catch (Exception e) {
            System.err.println("Error sending SMS to " + phoneNumber + ": " + e.getMessage());
        }
    }
    
    private String buildSmsContent(AlertMessage message) {
        // SMS tiene límite de 160 caracteres, así que hacemos el mensaje conciso
        String severity = message.getSeverity().name();
        String sensor = message.getSensorName();
        String time = message.getTimestamp().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Mensaje corto para SMS
        String shortMessage = String.format("ALERTA %s: %s - %s (%s)", 
                severity, sensor, message.getType(), time);
        
        // Si el mensaje es muy largo, lo truncamos
        if (shortMessage.length() > 140) {
            shortMessage = shortMessage.substring(0, 137) + "...";
        }
        
        return shortMessage;
    }
}
