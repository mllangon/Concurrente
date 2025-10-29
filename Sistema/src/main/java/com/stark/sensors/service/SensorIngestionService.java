package com.stark.sensors.service;

import com.stark.alerts.dto.AlertMessage;
import com.stark.alerts.service.AlertService;
import com.stark.sensors.config.SensorBeanConfig;
import com.stark.sensors.domain.Sensor;
import com.stark.sensors.domain.SensorEvent;
import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import com.stark.sensors.dto.SensorEventDto;
import com.stark.sensors.repo.SensorEventRepository;
import com.stark.sensors.repo.SensorRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorIngestionService {
    private final SensorRepository sensorRepo;
    private final SensorEventRepository eventRepo;
    private final AlertService alertService;
    private final MeterRegistry meterRegistry;
    private final Timer latencyTimer;
    private final Map<SensorType, SensorService> sensorServiceRegistry;
    private long totalEventsProcessed = 0;

    private final LinkedBlockingQueue<SensorEventDto> queue = new LinkedBlockingQueue<>();

    public SensorIngestionService(
            SensorRepository sensorRepo,
            SensorEventRepository eventRepo,
            AlertService alertService,
            MeterRegistry meterRegistry,
            @Autowired Map<SensorType, SensorService> sensorServiceRegistry) {
        this.sensorRepo = sensorRepo;
        this.eventRepo = eventRepo;
        this.alertService = alertService;
        this.meterRegistry = meterRegistry;
        this.latencyTimer = meterRegistry.timer("sensor.events.latency");
        this.sensorServiceRegistry = sensorServiceRegistry;
    }

    public void enqueue(SensorEventDto dto) {
        queue.offer(dto);
    }

    @Scheduled(fixedDelay = 50)
    public void drainQueue() {
        SensorEventDto dto;
        while ((dto = queue.poll()) != null) {
            ingestAsync(dto);
        }
    }

    @Async("sensorExecutor")
    @Transactional
    public void ingestAsync(SensorEventDto dto) {
        long start = System.nanoTime();
        try {
            Optional<Sensor> sensorOpt = sensorRepo.findById(dto.getSensorId());
            if (sensorOpt.isEmpty() || !sensorOpt.get().isActive()) {
                return; // drop invalid
            }
            Sensor sensor = sensorOpt.get();

            SensorEvent event = new SensorEvent();
            event.setSensorId(sensor.getId());
            event.setType(dto.getType());
            event.setValue(dto.getValue());

            // Usar severidad del DTO si está presente, o calcularla si no
            Severity severity = dto.getSeverity() != null ? dto.getSeverity() : evaluateSeverity(dto);
            event.setSeverity(severity);
            event.setTs(Instant.now());
            eventRepo.save(event);

            // Incrementar contador total
            totalEventsProcessed++;
            
            // Registrar métricas
            meterRegistry.counter("sensor.events.processed", "type", event.getType().name(), "severity", severity.name()).increment();
            meterRegistry.gauge("sensor.events.processed.total", totalEventsProcessed);

            if (severity == Severity.WARN || severity == Severity.CRITICAL) {
                AlertMessage msg = new AlertMessage(UUID.randomUUID(), sensor.getName(), event.getType(), severity,
                        buildAlertMessage(sensor, dto, severity), event.getTs());
                alertService.publish(msg);
            }
        } finally {
            latencyTimer.record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    private String buildAlertMessage(Sensor sensor, SensorEventDto dto, Severity s) {
        return "Sensor " + sensor.getName() + " [" + dto.getType() + "] => " + dto.getValue() + " (" + s + ")";
    }

    private Severity evaluateSeverity(SensorEventDto dto) {
        // Usar IoC para obtener el servicio específico del sensor
        SensorService sensorService = sensorServiceRegistry.get(dto.getType());
        
        if (sensorService == null) {
            return Severity.INFO; // Tipo de sensor no soportado
        }
        
        // Delegar el procesamiento al bean específico del sensor
        String severityString = sensorService.processEvent(dto);
        
        // Convertir string a enum Severity
        switch (severityString.toUpperCase()) {
            case "CRITICAL":
                return Severity.CRITICAL;
            case "WARN":
                return Severity.WARN;
            case "ERROR":
                return Severity.WARN; // Mapear ERROR a WARN
            default:
                return Severity.INFO;
        }
    }
    
    public long getTotalEventsProcessed() {
        return totalEventsProcessed;
    }
    
    public double getAverageLatency() {
        return latencyTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}




