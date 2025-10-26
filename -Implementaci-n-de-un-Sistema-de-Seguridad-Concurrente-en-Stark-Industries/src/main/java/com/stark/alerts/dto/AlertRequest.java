package com.stark.alerts.dto;

import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import java.util.List;
import java.util.UUID;

/**
 * DTO para solicitudes de alerta con destinatarios personalizados
 */
public class AlertRequest {
    private String sensorName;
    private SensorType sensorType;
    private Severity severity;
    private String message;
    private List<String> emailRecipients;
    private List<String> phoneNumbers;
    private List<String> deviceTokens;
    private List<String> services; // EMAIL, SMS, PUSH

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

    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    public List<String> getDeviceTokens() { return deviceTokens; }
    public void setDeviceTokens(List<String> deviceTokens) { this.deviceTokens = deviceTokens; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }
}
