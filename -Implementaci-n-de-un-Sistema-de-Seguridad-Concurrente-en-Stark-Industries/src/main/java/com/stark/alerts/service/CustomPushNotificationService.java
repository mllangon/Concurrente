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
 * Servicio de push notifications con tokens personalizados
 */
@Service
public class CustomPushNotificationService {
    
    private final ExecutorService executorService;
    
    public CustomPushNotificationService() {
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    /**
     * Envía push notifications a dispositivos específicos
     */
    public boolean sendAlertToDevices(AlertMessage message, List<String> deviceTokens) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            return false;
        }
        
        try {
            PushNotificationPayload payload = buildPushPayload(message);
            
            // Enviar push a todos los dispositivos especificados de forma asíncrona
            CompletableFuture.runAsync(() -> {
                for (String deviceToken : deviceTokens) {
                    sendPushToDevice(deviceToken, payload);
                }
            }, executorService);
            
            return true;
        } catch (Exception e) {
            System.err.println("Error sending push notification to custom devices: " + e.getMessage());
            return false;
        }
    }
    
    private void sendPushToDevice(String deviceToken, PushNotificationPayload payload) {
        try {
            // Simulación de envío de push notification
            // En producción, aquí se integraría con:
            // - Firebase Cloud Messaging (FCM) para Android
            // - Apple Push Notification Service (APNS) para iOS
            // - Web Push API para navegadores
            
            System.out.println("📲 Push enviado a dispositivo " + deviceToken.substring(0, Math.min(8, deviceToken.length())) + "...");
            System.out.println("   Título: " + payload.title);
            System.out.println("   Mensaje: " + payload.body);
            System.out.println("   Severidad: " + payload.severity);
            
            // Simular delay de red
            Thread.sleep(150);
            
        } catch (Exception e) {
            System.err.println("Error sending push to device " + deviceToken + ": " + e.getMessage());
        }
    }
    
    private PushNotificationPayload buildPushPayload(AlertMessage message) {
        String severity = message.getSeverity().name();
        String emoji = getSeverityEmoji(severity);
        
        return new PushNotificationPayload(
                emoji + " Alerta " + severity,
                message.getSensorName() + ": " + message.getMessage(),
                severity,
                message.getTimestamp().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
                message.getType().name()
        );
    }
    
    private String getSeverityEmoji(String severity) {
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> "🚨";
            case "WARN" -> "⚠️";
            case "INFO" -> "ℹ️";
            default -> "📢";
        };
    }
    
    /**
     * Clase interna para representar el payload de la notificación push
     */
    public static class PushNotificationPayload {
        public final String title;
        public final String body;
        public final String severity;
        public final String timestamp;
        public final String sensorType;
        
        public PushNotificationPayload(String title, String body, String severity, String timestamp, String sensorType) {
            this.title = title;
            this.body = body;
            this.severity = severity;
            this.timestamp = timestamp;
            this.sensorType = sensorType;
        }
    }
}
