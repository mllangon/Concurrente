package com.stark.alerts.service;

import com.stark.alerts.dto.AlertMessage;

/**
 * Interface para servicios de mensajería
 */
public interface MessagingService {
    
    /**
     * Envía una alerta a través del servicio de mensajería
     * @param message Mensaje de alerta a enviar
     * @return true si se envió correctamente, false en caso contrario
     */
    boolean sendAlert(AlertMessage message);
    
    /**
     * Obtiene el tipo de servicio de mensajería
     * @return Tipo de servicio (EMAIL, SMS, PUSH, etc.)
     */
    String getServiceType();
    
    /**
     * Verifica si el servicio está disponible
     * @return true si está disponible, false en caso contrario
     */
    boolean isAvailable();
}



