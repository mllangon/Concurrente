package com.stark.sensors.config;

import com.stark.sensors.domain.SensorType;
import com.stark.sensors.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuración de beans para sensores usando IoC
 * Gestiona el ciclo de vida de los beans de sensores
 */
@Configuration
public class SensorBeanConfig {
    
    @Autowired
    private List<SensorService> sensorServices;
    
    /**
     * Bean que mapea tipos de sensores con sus servicios correspondientes
     * Utiliza IoC para inyectar todos los servicios de sensores
     */
    @Bean(name = "sensorServiceRegistry")
    @Scope("singleton")
    public Map<SensorType, SensorService> sensorServiceRegistry() {
        Map<SensorType, SensorService> registry = new HashMap<>();
        
        // Spring IoC inyecta automáticamente todos los beans que implementan SensorService
        for (SensorService service : sensorServices) {
            registry.put(service.getSensorType(), service);
        }
        
        return registry;
    }
    
    /**
     * Bean para obtener servicios de sensores por tipo
     * Demuestra el uso de IoC para resolver dependencias
     */
    @Bean(name = "sensorServiceFactory")
    @Scope("singleton")
    public SensorServiceFactory sensorServiceFactory(Map<SensorType, SensorService> registry) {
        return new SensorServiceFactory(registry);
    }
    
    /**
     * Factory para obtener servicios de sensores
     * Implementa el patrón Factory usando IoC
     */
    public static class SensorServiceFactory {
        private final Map<SensorType, SensorService> registry;
        
        public SensorServiceFactory(Map<SensorType, SensorService> registry) {
            this.registry = registry;
        }
        
        public SensorService getSensorService(SensorType type) {
            return registry.get(type);
        }
        
        public boolean isSensorTypeSupported(SensorType type) {
            return registry.containsKey(type);
        }
    }
}



