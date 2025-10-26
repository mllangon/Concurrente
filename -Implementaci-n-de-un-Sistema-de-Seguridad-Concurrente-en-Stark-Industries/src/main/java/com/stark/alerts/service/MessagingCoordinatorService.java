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
    private final CustomSmsMessagingService customSmsService;
    private final CustomPushNotificationService customPushService;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public MessagingCoordinatorService(List<MessagingService> messagingServices,
                                      CustomEmailMessagingService customEmailService,
                                      CustomSmsMessagingService customSmsService,
                                      CustomPushNotificationService customPushService,
                                      MeterRegistry meterRegistry) {
        this.messagingServices = messagingServices;
        this.customEmailService = customEmailService;
        this.customSmsService = customSmsService;
        this.customPushService = customPushService;
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
        
        // Enviar a emails personalizados
        if (request.getEmailRecipients() != null && !request.getEmailRecipients().isEmpty()) {
            try {
                boolean success = customEmailService.sendAlertToRecipients(message, request.getEmailRecipients());
                results.put("EMAIL_CUSTOM", success);
                meterRegistry.counter("messaging.sent", 
                        "service", "EMAIL_CUSTOM",
                        "severity", message.getSeverity().name(),
                        "success", String.valueOf(success))
                        .increment();
            } catch (Exception e) {
                results.put("EMAIL_CUSTOM", false);
                meterRegistry.counter("messaging.error", 
                        "service", "EMAIL_CUSTOM",
                        "severity", message.getSeverity().name())
                        .increment();
            }
        }
        
        // Enviar a números SMS personalizados
        if (request.getPhoneNumbers() != null && !request.getPhoneNumbers().isEmpty()) {
            try {
                boolean success = customSmsService.sendAlertToNumbers(message, request.getPhoneNumbers());
                results.put("SMS_CUSTOM", success);
                meterRegistry.counter("messaging.sent", 
                        "service", "SMS_CUSTOM",
                        "severity", message.getSeverity().name(),
                        "success", String.valueOf(success))
                        .increment();
            } catch (Exception e) {
                results.put("SMS_CUSTOM", false);
                meterRegistry.counter("messaging.error", 
                        "service", "SMS_CUSTOM",
                        "severity", message.getSeverity().name())
                        .increment();
            }
        }
        
        // Enviar a tokens push personalizados
        if (request.getDeviceTokens() != null && !request.getDeviceTokens().isEmpty()) {
            try {
                boolean success = customPushService.sendAlertToDevices(message, request.getDeviceTokens());
                results.put("PUSH_CUSTOM", success);
                meterRegistry.counter("messaging.sent", 
                        "service", "PUSH_CUSTOM",
                        "severity", message.getSeverity().name(),
                        "success", String.valueOf(success))
                        .increment();
            } catch (Exception e) {
                results.put("PUSH_CUSTOM", false);
                meterRegistry.counter("messaging.error", 
                        "service", "PUSH_CUSTOM",
                        "severity", message.getSeverity().name())
                        .increment();
            }
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
