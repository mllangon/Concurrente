package com.stark.monitoring.web;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsPublicController {
    private final MeterRegistry meterRegistry;

    public MetricsPublicController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Obtener todas las métricas con sus valores
        for (Meter meter : meterRegistry.getMeters()) {
            String meterName = meter.getId().getName();
            Object value = getMeterValue(meter);
            if (value != null) {
                metrics.put(meterName, value);
            }
        }
        
        // Agregar métricas específicas de sensores si no existen
        if (!metrics.containsKey("sensor_events_processed_total")) {
            metrics.put("sensor_events_processed_total", 0);
        }
        if (!metrics.containsKey("sensor_events_latency_seconds")) {
            metrics.put("sensor_events_latency_seconds", 0.0);
        }
        
        return ResponseEntity.ok(metrics);
    }
    
    private Object getMeterValue(Meter meter) {
        try {
            return meter.measure().stream()
                    .mapToDouble(measurement -> measurement.getValue())
                    .findFirst()
                    .orElse(0.0);
        } catch (Exception e) {
            return null;
        }
    }
}



