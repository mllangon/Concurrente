package com.stark.alerts.web;

import com.stark.alerts.dto.AlertMessage;
import com.stark.alerts.service.AlertService;
import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Controlador para probar las alertas en tiempo real
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertTestController {
    
    private final AlertService alertService;
    
    @Autowired
    public AlertTestController(AlertService alertService) {
        this.alertService = alertService;
    }
    
    /**
     * Envía una alerta de prueba para verificar WebSocket
     */
    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER')")
    public String sendTestAlert(@RequestParam(defaultValue = "CRITICAL") String severity) {
        AlertMessage alert = new AlertMessage(
            UUID.randomUUID(),
            "Sensor-Test",
            SensorType.MOTION,
            Severity.valueOf(severity),
            "Alerta de prueba - " + severity + " - " + Instant.now(),
            Instant.now()
        );
        
        alertService.publish(alert);
        return "Alerta de prueba enviada: " + severity;
    }
}

