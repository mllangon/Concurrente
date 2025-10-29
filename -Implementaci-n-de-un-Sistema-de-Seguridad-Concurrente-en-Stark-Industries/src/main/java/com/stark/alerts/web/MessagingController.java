package com.stark.alerts.web;

import com.stark.alerts.dto.AlertMessage;
import com.stark.alerts.dto.AlertRequest;
import com.stark.alerts.service.AlertService;
import com.stark.alerts.service.MessagingCoordinatorService;
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
    
    public MessagingController(AlertService alertService,
                               MessagingCoordinatorService messagingCoordinator) {
        this.alertService = alertService;
        this.messagingCoordinator = messagingCoordinator;
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getServicesStatus() {
        return ResponseEntity.ok(alertService.getMessagingServicesStatus());
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServicesInfo() {
        return ResponseEntity.ok(alertService.getMessagingServicesInfo());
    }
    
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
    
    @PostMapping("/send-custom")
    public ResponseEntity<Map<String, Object>> sendCustomAlert(@RequestBody AlertRequest request) {
        try {
            AlertMessage message = new AlertMessage(
                    UUID.randomUUID(),
                    request.getSensorName(),
                    request.getSensorType(),
                    request.getSeverity(),
                    request.getMessage(),
                    Instant.now()
            );
            
            alertService.publish(message);
            
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
                            "emails", request.getEmailRecipients() != null ? request.getEmailRecipients().size() : 0
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
