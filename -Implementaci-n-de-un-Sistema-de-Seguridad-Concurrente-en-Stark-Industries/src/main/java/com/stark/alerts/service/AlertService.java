package com.stark.alerts.service;

import com.stark.alerts.dto.AlertMessage;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    private final MeterRegistry meterRegistry;
    private final String emailTo;

    public AlertService(SimpMessagingTemplate messagingTemplate, JavaMailSender mailSender, MeterRegistry meterRegistry,
                        @Value("${app.alerts.email-to}") String emailTo) {
        this.messagingTemplate = messagingTemplate;
        this.mailSender = mailSender;
        this.meterRegistry = meterRegistry;
        this.emailTo = emailTo;
    }

    public void publish(AlertMessage message) {
        messagingTemplate.convertAndSend("/topic/alerts", message);
        meterRegistry.counter("alerts.published", "severity", message.getSeverity().name()).increment();
        sendEmailAsync(message);
    }

    @Async("sensorExecutor")
    public void sendEmailAsync(AlertMessage message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(emailTo);
            mail.setSubject("[ALERTA] " + message.getSeverity() + " - " + message.getSensorName());
            mail.setText(message.getMessage());
            mailSender.send(mail);
        } catch (Exception e) {
            // Log error but don't fail the alert processing
            System.err.println("Error sending email alert: " + e.getMessage());
        }
    }
}




