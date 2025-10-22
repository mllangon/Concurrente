package com.stark.sensors.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class SensorEvent {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID sensorId;

    @Enumerated(EnumType.STRING)
    private SensorType type;

    @Column(name = "event_value")
    private String value;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private Instant ts = Instant.now();

    public enum Severity { INFO, WARN, CRITICAL }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSensorId() { return sensorId; }
    public void setSensorId(UUID sensorId) { this.sensorId = sensorId; }
    public SensorType getType() { return type; }
    public void setType(SensorType type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Instant getTs() { return ts; }
    public void setTs(Instant ts) { this.ts = ts; }
}



