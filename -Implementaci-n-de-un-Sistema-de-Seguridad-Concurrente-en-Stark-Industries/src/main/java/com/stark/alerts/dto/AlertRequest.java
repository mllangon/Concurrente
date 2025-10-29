package com.stark.alerts.dto;

import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import java.util.List;

/**
 * DTO para solicitudes de alerta con destinatarios de email personalizados
 */
public class AlertRequest {
    private String sensorName;
    private SensorType sensorType;
    private Severity severity;
    private String message;
    private List<String> emailRecipients;

    public AlertRequest() {}

    public AlertRequest(String sensorName, SensorType sensorType, Severity severity, String message) {
        this.sensorName = sensorName;
        this.sensorType = sensorType;
        this.severity = severity;
        this.message = message;
    }

    // Getters y Setters
    public String getSensorName() { return sensorName; }
    public void setSensorName(String sensorName) { this.sensorName = sensorName; }

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getEmailRecipients() { return emailRecipients; }
    public void setEmailRecipients(List<String> emailRecipients) { this.emailRecipients = emailRecipients; }
}



