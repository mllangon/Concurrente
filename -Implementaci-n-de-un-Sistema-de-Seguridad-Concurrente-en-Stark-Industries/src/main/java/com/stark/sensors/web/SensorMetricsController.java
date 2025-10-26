package com.stark.sensors.web;

import com.stark.sensors.service.SensorIngestionService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
public class SensorMetricsController {
    private final SensorIngestionService sensorIngestionService;

    public SensorMetricsController(SensorIngestionService sensorIngestionService) {
        this.sensorIngestionService = sensorIngestionService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSensorMetrics() {
        long totalEvents = sensorIngestionService.getTotalEventsProcessed();
        double averageLatency = sensorIngestionService.getAverageLatency();
        
        return ResponseEntity.ok(Map.of(
            "sensor_events_processed_total", totalEvents,
            "sensor_events_latency_seconds", averageLatency / 1000.0,
            "sensor_events_latency_ms", averageLatency
        ));
    }
}

