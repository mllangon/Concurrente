package com.stark.alerts.web;

import com.stark.alerts.dto.AlertMessage;
import com.stark.alerts.dto.AlertRequest;
import com.stark.alerts.service.AlertService;
import com.stark.alerts.service.MessagingCoordinatorService;
import com.stark.alerts.service.SmsMessagingService;
import com.stark.alerts.service.PushNotificationService;
import com.stark.sensors.domain.SensorType;
import com.stark.sensors.domain.SensorEvent.Severity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador para gestionar servicios de mensajería
 */
@RestController
@RequestMapping("/api/messaging")
public class MessagingController {
    
    private final AlertService alertService;
    private final MessagingCoordinatorService messagingCoordinator;
    private final SmsMessagingService smsService;
    private final PushNotificationService pushService;
    
    public MessagingController(AlertService alertService,
                              MessagingCoordinatorService messagingCoordinator,
                              SmsMessagingService smsService,
                              PushNotificationService pushService) {
        this.alertService = alertService;
        this.messagingCoordinator = messagingCoordinator;
        this.smsService = smsService;
        this.pushService = pushService;
    }
    
    /**
     * Obtiene el estado de todos los servicios de mensajería
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getServicesStatus() {
        return ResponseEntity.ok(alertService.getMessagingServicesStatus());
    }
    
    /**
     * Obtiene información detallada de los servicios
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServicesInfo() {
        return ResponseEntity.ok(alertService.getMessagingServicesInfo());
    }
    
    /**
     * Envía una alerta de prueba a todos los servicios
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestAlert(@RequestParam(defaultValue = "CRITICAL") String severity) {
        AlertMessage testMessage = new AlertMessage(
                UUID.randomUUID(),
                "Sensor de Prueba",
                SensorType.TEMPERATURE,
                Severity.valueOf(severity.toUpperCase()),
                "Esta es una alerta de prueba del sistema de mensajería",
                Instant.now()
        );
        
        alertService.publish(testMessage);
        
        return ResponseEntity.ok(Map.of(
                "message", "Alerta de prueba enviada",
                "severity", severity,
                "services", alertService.getMessagingServicesStatus()
        ));
    }
    
    /**
     * Envía una alerta a servicios específicos
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendAlertToServices(
            @RequestParam String severity,
            @RequestParam String sensorName,
            @RequestParam String message,
            @RequestParam List<String> services) {
        
        AlertMessage alertMessage = new AlertMessage(
                UUID.randomUUID(),
                sensorName,
                SensorType.TEMPERATURE,
                Severity.valueOf(severity.toUpperCase()),
                message,
                Instant.now()
        );
        
        alertService.publishToServices(alertMessage, services);
        
        return ResponseEntity.ok(Map.of(
                "message", "Alerta enviada a servicios específicos",
                "services", services,
                "alert", Map.of(
                        "severity", severity,
                        "sensor", sensorName,
                        "message", message
                )
        ));
    }
    
    /**
     * Gestiona números de teléfono para SMS
     */
    @PostMapping("/sms/phone")
    public ResponseEntity<Map<String, Object>> addPhoneNumber(@RequestParam String phoneNumber) {
        smsService.addPhoneNumber(phoneNumber);
        
        return ResponseEntity.ok(Map.of(
                "message", "Número de teléfono agregado",
                "phoneNumber", phoneNumber,
                "totalPhones", smsService.getPhoneNumbers().size()
        ));
    }
    
    @GetMapping("/sms/phones")
    public ResponseEntity<List<String>> getPhoneNumbers() {
        return ResponseEntity.ok(smsService.getPhoneNumbers());
    }
    
    /**
     * Gestiona tokens de dispositivos para push notifications
     */
    @PostMapping("/push/device")
    public ResponseEntity<Map<String, Object>> addDeviceToken(@RequestParam String deviceToken) {
        pushService.addDeviceToken(deviceToken);
        
        return ResponseEntity.ok(Map.of(
                "message", "Token de dispositivo agregado",
                "deviceToken", deviceToken.substring(0, Math.min(8, deviceToken.length())) + "...",
                "totalDevices", pushService.getDeviceTokens().size()
        ));
    }
    
    @GetMapping("/push/devices")
    public ResponseEntity<List<String>> getDeviceTokens() {
        return ResponseEntity.ok(pushService.getDeviceTokens());
    }
    
    /**
     * Envía una alerta con destinatarios personalizados
     */
    @PostMapping("/send-custom")
    public ResponseEntity<Map<String, Object>> sendCustomAlert(@RequestBody AlertRequest request) {
        try {
            // Enviar por WebSocket (tiempo real)
            AlertMessage message = new AlertMessage(
                    UUID.randomUUID(),
                    request.getSensorName(),
                    request.getSensorType(),
                    request.getSeverity(),
                    request.getMessage(),
                    Instant.now()
            );
            
            alertService.publish(message);
            
            // Enviar a destinatarios personalizados
            Map<String, Boolean> results = messagingCoordinator.sendCustomAlert(request).get();
            
            return ResponseEntity.ok(Map.of(
                    "message", "Alerta enviada con destinatarios personalizados",
                    "alert", Map.of(
                            "severity", request.getSeverity(),
                            "sensor", request.getSensorName(),
                            "message", request.getMessage()
                    ),
                    "results", results,
                    "recipients", Map.of(
                            "emails", request.getEmailRecipients() != null ? request.getEmailRecipients().size() : 0,
                            "phones", request.getPhoneNumbers() != null ? request.getPhoneNumbers().size() : 0,
                            "devices", request.getDeviceTokens() != null ? request.getDeviceTokens().size() : 0
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error enviando alerta personalizada",
                    "message", e.getMessage()
            ));
        }
    }
}
