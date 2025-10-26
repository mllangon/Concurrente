package com.stark.sensors.service;

import com.stark.sensors.domain.SensorType;
import com.stark.sensors.dto.SensorEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;

/**
 * Servicio específico para sensores de temperatura
 * Bean gestionado por Spring con IoC
 */
@Service("temperatureSensorService")
@Scope("singleton")
public class TemperatureSensorService implements SensorService {
    
    private final int warnThreshold;
    private final int criticalHigh;
    private final int criticalLow;
    
    public TemperatureSensorService(
            @Value("${app.sensors.temperature.warn-threshold}") int warnThreshold,
            @Value("${app.sensors.temperature.critical-high}") int criticalHigh,
            @Value("${app.sensors.temperature.critical-low}") int criticalLow) {
        this.warnThreshold = warnThreshold;
        this.criticalHigh = criticalHigh;
        this.criticalLow = criticalLow;
    }
    
    @Override
    public SensorType getSensorType() {
        return SensorType.TEMPERATURE;
    }
    
    @Override
    public String processEvent(SensorEventDto dto) {
        try {
            double temperature = Double.parseDouble(dto.getValue());
            
            if (temperature >= criticalHigh || temperature <= criticalLow) {
                return "CRITICAL";
            } else if (temperature >= warnThreshold) {
                return "WARN";
            }
            return "NORMAL";
        } catch (NumberFormatException e) {
            return "ERROR";
        }
    }
    
    @Override
    public String getSensorDescription() {
        return "Sensor de temperatura con umbrales configurables";
    }
}


