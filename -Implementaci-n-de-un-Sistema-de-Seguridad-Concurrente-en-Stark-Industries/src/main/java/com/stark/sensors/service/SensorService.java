package com.stark.sensors.service;

import com.stark.sensors.domain.SensorType;
import com.stark.sensors.dto.SensorEventDto;

/**
 * Interfaz común para todos los servicios de sensores
 * Define el contrato para beans de sensores gestionados por IoC
 */
public interface SensorService {
    
    /**
     * Obtiene el tipo de sensor que maneja este servicio
     * @return SensorType del sensor
     */
    SensorType getSensorType();
    
    /**
     * Procesa un evento de sensor y determina su severidad
     * @param dto Evento del sensor
     * @return Severidad del evento (NORMAL, WARN, CRITICAL, ERROR)
     */
    String processEvent(SensorEventDto dto);
    
    /**
     * Obtiene la descripción del sensor
     * @return Descripción del sensor
     */
    String getSensorDescription();
}

