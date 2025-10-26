package com.stark.sensors.service;

import com.stark.sensors.domain.SensorType;
import com.stark.sensors.dto.SensorEventDto;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;

/**
 * Servicio específico para sensores de acceso
 * Bean gestionado por Spring con IoC
 */
@Service("accessSensorService")
@Scope("singleton")
public class AccessSensorService implements SensorService {
    
    @Override
    public SensorType getSensorType() {
        return SensorType.ACCESS;
    }
    
    @Override
    public String processEvent(SensorEventDto dto) {
        String value = dto.getValue();
        
        if ("UNAUTHORIZED".equals(value)) {
            return "CRITICAL"; // Acceso no autorizado
        } else if ("DENIED".equals(value)) {
            return "WARN"; // Acceso denegado
        } else if ("GRANTED".equals(value)) {
            return "NORMAL"; // Acceso autorizado
        }
        return "NORMAL";
    }
    
    @Override
    public String getSensorDescription() {
        return "Sensor de control de acceso con validación de autorización";
    }
}

