package com.stark.alerts.service;

import com.stark.alerts.dto.AlertMessage;
import com.stark.alerts.dto.AlertRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Servicio coordinador que gestiona todos los servicios de mensajería
 */
@Service
public class MessagingCoordinatorService {
    
    private final List<MessagingService> messagingServices;
    private final CustomEmailMessagingService customEmailService;
    private final EmailConfigurationService emailConfigService;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public MessagingCoordinatorService(List<MessagingService> messagingServices,
                                       CustomEmailMessagingService customEmailService,
                                       EmailConfigurationService emailConfigService,
                                       MeterRegistry meterRegistry) {
        this.messagingServices = messagingServices;
        this.customEmailService = customEmailService;
        this.emailConfigService = emailConfigService;
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Envía una alerta a través de todos los servicios de mensajería disponibles
     */
    @Async("sensorExecutor")
    public CompletableFuture<Map<String, Boolean>> sendAlertToAllServices(AlertMessage message) {
        Map<String, Boolean> results = messagingServices.stream()
                .filter(MessagingService::isAvailable)
                .collect(Collectors.toMap(
                        MessagingService::getServiceType,
                        service -> {
                            try {
                                boolean success = service.sendAlert(message);
                                // Registrar métricas
                                meterRegistry.counter("messaging.sent", 
                                        "service", service.getServiceType(),
                                        "severity", message.getSeverity().name(),
                                        "success", String.valueOf(success))
                                        .increment();
                                return success;
                            } catch (Exception e) {
                                System.err.println("Error in messaging service " + service.getServiceType() + ": " + e.getMessage());
                                meterRegistry.counter("messaging.error", 
                                        "service", service.getServiceType(),
                                        "severity", message.getSeverity().name())
                                        .increment();
                                return false;
                            }
                        }
                ));
        
        return CompletableFuture.completedFuture(results);
    }
    
    /**
     * Envía una alerta solo a servicios específicos
     */
    @Async("sensorExecutor")
    public CompletableFuture<Map<String, Boolean>> sendAlertToServices(AlertMessage message, List<String> serviceTypes) {
        Map<String, Boolean> results = messagingServices.stream()
                .filter(service -> serviceTypes.contains(service.getServiceType()))
                .filter(MessagingService::isAvailable)
                .collect(Collectors.toMap(
                        MessagingService::getServiceType,
                        service -> {
                            try {
                                boolean success = service.sendAlert(message);
                                meterRegistry.counter("messaging.sent", 
                                        "service", service.getServiceType(),
                                        "severity", message.getSeverity().name(),
                                        "success", String.valueOf(success))
                                        .increment();
                                return success;
                            } catch (Exception e) {
                                System.err.println("Error in messaging service " + service.getServiceType() + ": " + e.getMessage());
                                meterRegistry.counter("messaging.error", 
                                        "service", service.getServiceType(),
                                        "severity", message.getSeverity().name())
                                        .increment();
                                return false;
                            }
                        }
                ));
        
        return CompletableFuture.completedFuture(results);
    }
    
    /**
     * Obtiene el estado de todos los servicios de mensajería
     */
    public Map<String, Boolean> getServicesStatus() {
        return messagingServices.stream()
                .collect(Collectors.toMap(
                        MessagingService::getServiceType,
                        MessagingService::isAvailable
                ));
    }
    
    /**
     * Envía una alerta con destinatarios personalizados
     */
    @Async("sensorExecutor")
    public CompletableFuture<Map<String, Boolean>> sendCustomAlert(AlertRequest request) {
        // Crear AlertMessage desde AlertRequest
        AlertMessage message = new AlertMessage(
                UUID.randomUUID(),
                request.getSensorName(),
                request.getSensorType(),
                request.getSeverity(),
                request.getMessage(),
                Instant.now()
        );
        
        Map<String, Boolean> results = new java.util.HashMap<>();
        
        // Si no se especifican destinatarios, usar configuración por defecto
        List<String> emailRecipients = request.getEmailRecipients();
        if (emailRecipients == null || emailRecipients.isEmpty()) {
            // Usar email de destino por defecto de la configuración
            String defaultEmail = emailConfigService.getEmailTo();
            emailRecipients = List.of(defaultEmail);
        }
        
        // Enviar a emails (personalizados o por defecto)
        try {
            boolean success = customEmailService.sendAlertToRecipients(message, emailRecipients);
            results.put("EMAIL", success);
            meterRegistry.counter("messaging.sent", 
                    "service", "EMAIL",
                    "severity", message.getSeverity().name(),
                    "success", String.valueOf(success))
                    .increment();
        } catch (Exception e) {
            results.put("EMAIL", false);
            meterRegistry.counter("messaging.error", 
                    "service", "EMAIL",
                    "severity", message.getSeverity().name())
                    .increment();
        }
        
        
        return CompletableFuture.completedFuture(results);
    }
    
    /**
     * Obtiene información detallada de los servicios
     */
    public Map<String, Object> getServicesInfo() {
        return messagingServices.stream()
                .collect(Collectors.toMap(
                        MessagingService::getServiceType,
                        service -> Map.of(
                                "available", service.isAvailable(),
                                "type", service.getServiceType()
                        )
                ));
    }
}
