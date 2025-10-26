package com.stark.alerts.service;

import com.stark.alerts.dto.AlertMessage;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AlertService {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessagingCoordinatorService messagingCoordinator;
    private final MeterRegistry meterRegistry;

    public AlertService(SimpMessagingTemplate messagingTemplate, 
                       MessagingCoordinatorService messagingCoordinator,
                       MeterRegistry meterRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.messagingCoordinator = messagingCoordinator;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Publica una alerta a través de WebSocket y todos los servicios de mensajería
     */
    public void publish(AlertMessage message) {
        // Enviar por WebSocket (tiempo real)
        messagingTemplate.convertAndSend("/topic/alerts", message);
        meterRegistry.counter("alerts.published", "severity", message.getSeverity().name()).increment();
        
        // Enviar por todos los servicios de mensajería disponibles
        messagingCoordinator.sendAlertToAllServices(message);
    }

    /**
     * Publica una alerta solo a servicios específicos
     */
    public void publishToServices(AlertMessage message, List<String> serviceTypes) {
        // Enviar por WebSocket (tiempo real)
        messagingTemplate.convertAndSend("/topic/alerts", message);
        meterRegistry.counter("alerts.published", "severity", message.getSeverity().name()).increment();
        
        // Enviar solo a servicios específicos
        messagingCoordinator.sendAlertToServices(message, serviceTypes);
    }

    /**
     * Obtiene el estado de los servicios de mensajería
     */
    public Map<String, Boolean> getMessagingServicesStatus() {
        return messagingCoordinator.getServicesStatus();
    }

    /**
     * Obtiene información detallada de los servicios
     */
    public Map<String, Object> getMessagingServicesInfo() {
        return messagingCoordinator.getServicesInfo();
    }
}




