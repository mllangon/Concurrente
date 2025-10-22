package com.stark.sensors.dto;

import com.stark.sensors.domain.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SensorEventDto {
    @NotNull private UUID sensorId;
    @NotNull private SensorType type;
    @NotBlank private String value;

    public UUID getSensorId() { return sensorId; }
    public void setSensorId(UUID sensorId) { this.sensorId = sensorId; }
    public SensorType getType() { return type; }
    public void setType(SensorType type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}




