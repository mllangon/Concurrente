package com.stark.sensors.service;

import com.stark.alerts.dto.AlertMessage;
import com.stark.alerts.service.AlertService;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.beans.factory.annotation.Value;
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

    private final LinkedBlockingQueue<SensorEventDto> queue = new LinkedBlockingQueue<>();

    private final LocalTime offStart;
    private final LocalTime offEnd;
    private final int warnTemp;
    private final int criticalHigh;
    private final int criticalLow;

    public SensorIngestionService(
            SensorRepository sensorRepo,
            SensorEventRepository eventRepo,
            AlertService alertService,
            MeterRegistry meterRegistry,
            @Value("${app.sensors.motion.off-hours.start}") String offStart,
            @Value("${app.sensors.motion.off-hours.end}") String offEnd,
            @Value("${app.sensors.temperature.warn-threshold}") int warnTemp,
            @Value("${app.sensors.temperature.critical-high}") int criticalHigh,
            @Value("${app.sensors.temperature.critical-low}") int criticalLow) {
        this.sensorRepo = sensorRepo;
        this.eventRepo = eventRepo;
        this.alertService = alertService;
        this.meterRegistry = meterRegistry;
        this.latencyTimer = meterRegistry.timer("sensor.events.latency");
        this.offStart = LocalTime.parse(offStart);
        this.offEnd = LocalTime.parse(offEnd);
        this.warnTemp = warnTemp;
        this.criticalHigh = criticalHigh;
        this.criticalLow = criticalLow;
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

            Severity severity = evaluateSeverity(dto);
            event.setSeverity(severity);
            event.setTs(Instant.now());
            eventRepo.save(event);

            meterRegistry.counter("sensor.events.processed", "type", event.getType().name(), "severity", severity.name()).increment();

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
        if (dto.getType() == SensorType.MOTION) {
            boolean motion = "MOTION_DETECTED".equalsIgnoreCase(dto.getValue());
            if (motion) {
                if (isOffHours()) return Severity.CRITICAL; else return Severity.WARN;
            }
            return Severity.INFO;
        }
        if (dto.getType() == SensorType.TEMPERATURE) {
            try {
                int temp = Integer.parseInt(dto.getValue());
                if (temp < criticalLow || temp > criticalHigh) return Severity.CRITICAL;
                if (temp >= warnTemp) return Severity.WARN;
                return Severity.INFO;
            } catch (NumberFormatException ex) {
                return Severity.WARN;
            }
        }
        if (dto.getType() == SensorType.ACCESS) {
            boolean authorized = Boolean.parseBoolean(dto.getValue());
            return authorized ? Severity.INFO : Severity.CRITICAL;
        }
        return Severity.INFO;
    }

    private boolean isOffHours() {
        LocalTime now = LocalTime.now();
        if (offStart.isBefore(offEnd)) {
            return now.isAfter(offStart) && now.isBefore(offEnd);
        } else {
            return now.isAfter(offStart) || now.isBefore(offEnd);
        }
    }
}




