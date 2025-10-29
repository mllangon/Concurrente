package com.stark.alerts.dto;

import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO para mensajes de alerta
 */
public class AlertMessage {
    private UUID id;
    private String sensorName;
    private SensorType type;
    private Severity severity;
    private String message;
    private Instant timestamp;

    public AlertMessage() {}

    public AlertMessage(UUID id, String sensorName, SensorType type, Severity severity, String message, Instant timestamp) {
        this.id = id;
        this.sensorName = sensorName;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.timestamp = timestamp;
    }

    public AlertMessage(String sensorName, SensorType type, Severity severity, String message, Instant timestamp) {
        this(UUID.randomUUID(), sensorName, type, severity, message, timestamp);
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSensorName() { return sensorName; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public SensorType getType() { return type; }
    public void setType(SensorType type) { this.type = type; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}