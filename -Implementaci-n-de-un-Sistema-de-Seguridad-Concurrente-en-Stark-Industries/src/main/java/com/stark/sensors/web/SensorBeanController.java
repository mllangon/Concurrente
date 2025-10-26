package com.stark.sensors.web;

import com.stark.sensors.config.SensorBeanConfig;
import com.stark.sensors.domain.SensorType;
import com.stark.sensors.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para demostrar el uso de beans de sensores con IoC
 * Muestra cómo Spring gestiona el ciclo de vida de los beans
 */
@RestController
@RequestMapping("/api/sensor-beans")
public class SensorBeanController {
    
    private final Map<SensorType, SensorService> sensorServiceRegistry;
    private final SensorBeanConfig.SensorServiceFactory sensorServiceFactory;
    
    @Autowired
    public SensorBeanController(
            Map<SensorType, SensorService> sensorServiceRegistry,
            SensorBeanConfig.SensorServiceFactory sensorServiceFactory) {
        this.sensorServiceRegistry = sensorServiceRegistry;
        this.sensorServiceFactory = sensorServiceFactory;
    }
    
    /**
     * Obtiene información de todos los beans de sensores registrados
     * Demuestra el uso de IoC para resolver dependencias
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER')")
    public Map<String, Object> getSensorBeansInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Información de cada bean de sensor
        for (Map.Entry<SensorType, SensorService> entry : sensorServiceRegistry.entrySet()) {
            SensorType type = entry.getKey();
            SensorService service = entry.getValue();
            
            Map<String, Object> sensorInfo = new HashMap<>();
            sensorInfo.put("type", type.name());
            sensorInfo.put("description", service.getSensorDescription());
            sensorInfo.put("beanName", service.getClass().getSimpleName());
            sensorInfo.put("scope", "singleton"); // Todos nuestros beans son singleton
            
            info.put(type.name().toLowerCase() + "Sensor", sensorInfo);
        }
        
        // Información general del registro
        info.put("totalBeans", sensorServiceRegistry.size());
        info.put("supportedTypes", sensorServiceRegistry.keySet());
        
        return info;
    }
    
    /**
     * Prueba un bean específico de sensor
     * Demuestra la resolución de dependencias por tipo
     */
    @GetMapping("/test/{sensorType}")
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER')")
    public Map<String, Object> testSensorBean(@PathVariable SensorType sensorType) {
        Map<String, Object> result = new HashMap<>();
        
        // Verificar si el tipo de sensor está soportado
        boolean isSupported = sensorServiceFactory.isSensorTypeSupported(sensorType);
        result.put("supported", isSupported);
        
        if (isSupported) {
            // Obtener el servicio específico usando IoC
            SensorService service = sensorServiceFactory.getSensorService(sensorType);
            result.put("beanName", service.getClass().getSimpleName());
            result.put("description", service.getSensorDescription());
            result.put("type", service.getSensorType().name());
        }
        
        return result;
    }
    
    /**
     * Obtiene estadísticas de los beans de sensores
     * Demuestra el ciclo de vida de los beans
     */
    @GetMapping("/lifecycle")
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER')")
    public Map<String, Object> getBeanLifecycleInfo() {
        Map<String, Object> lifecycle = new HashMap<>();
        
        lifecycle.put("totalBeans", sensorServiceRegistry.size());
        lifecycle.put("beanScope", "singleton");
        lifecycle.put("lifecycleManaged", "Spring IoC Container");
        lifecycle.put("initialization", "Eager (at startup)");
        lifecycle.put("dependencyInjection", "Constructor-based");
        
        // Información de cada bean
        Map<String, Object> beans = new HashMap<>();
        for (Map.Entry<SensorType, SensorService> entry : sensorServiceRegistry.entrySet()) {
            SensorService service = entry.getValue();
            Map<String, Object> beanInfo = new HashMap<>();
            beanInfo.put("class", service.getClass().getName());
            beanInfo.put("scope", "singleton");
            beanInfo.put("initialized", true);
            beans.put(entry.getKey().name(), beanInfo);
        }
        lifecycle.put("beans", beans);
        
        return lifecycle;
    }
}


