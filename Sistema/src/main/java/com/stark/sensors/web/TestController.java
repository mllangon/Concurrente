package com.stark.sensors.web;

import com.stark.sensors.domain.Sensor;
import com.stark.sensors.domain.SensorEvent;
import com.stark.sensors.domain.SensorType;
import com.stark.sensors.dto.SensorEventDto;
import com.stark.sensors.repo.SensorRepository;
import com.stark.sensors.service.SensorIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final SensorIngestionService sensorIngestionService;
    private final SensorRepository sensorRepository;

    public TestController(SensorIngestionService sensorIngestionService, SensorRepository sensorRepository) {
        this.sensorIngestionService = sensorIngestionService;
        this.sensorRepository = sensorRepository;
    }

    @PostMapping("/generate-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateTestEvents(@RequestParam(defaultValue = "100") int count) {
        try {
            // Obtener sensores disponibles
            List<Sensor> sensors = sensorRepository.findAll();
            if (sensors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No hay sensores disponibles para generar eventos"
                ));
            }

            // Tipos de eventos y severidades
            SensorType[] types = {SensorType.TEMPERATURE, SensorType.MOTION, SensorType.ACCESS};
            String[] severities = {"INFO", "WARN", "CRITICAL"};
            String[] values = {"test", "detected", "high", "low", "normal", "alert", "warning", "critical"};

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            // Generar eventos de forma asíncrona
            for (int i = 0; i < count; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Seleccionar sensor aleatorio
                        Sensor randomSensor = sensors.get(new Random().nextInt(sensors.size()));
                        
                        // Crear evento aleatorio
                        SensorEventDto eventDto = new SensorEventDto();
                        eventDto.setSensorId(randomSensor.getId());
                        eventDto.setType(types[new Random().nextInt(types.length)]);
                        eventDto.setValue(values[new Random().nextInt(values.length)]);
                        
                        // Convertir string a enum Severity
                        String severityStr = severities[new Random().nextInt(severities.length)];
                        SensorEvent.Severity severity;
                        switch (severityStr) {
                            case "CRITICAL":
                                severity = SensorEvent.Severity.CRITICAL;
                                break;
                            case "WARN":
                                severity = SensorEvent.Severity.WARN;
                                break;
                            default:
                                severity = SensorEvent.Severity.INFO;
                                break;
                        }
                        eventDto.setSeverity(severity);

                        // Enviar evento usando el método correcto
                        sensorIngestionService.enqueue(eventDto);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Error generando evento: " + e.getMessage());
                    }
                });
                futures.add(future);
            }

            // Esperar a que todos los eventos se procesen
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Generación de eventos completada",
                "totalRequested", count,
                "successful", successCount.get(),
                "errors", errorCount.get(),
                "durationMs", duration,
                "eventsPerSecond", String.format("%.2f", (double) successCount.get() / (duration / 1000.0))
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error generando eventos: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/sensors/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSensorCount() {
        long count = sensorRepository.count();
        return ResponseEntity.ok(Map.of(
            "sensorCount", count,
            "message", "Total de sensores disponibles: " + count
        ));
    }
}
