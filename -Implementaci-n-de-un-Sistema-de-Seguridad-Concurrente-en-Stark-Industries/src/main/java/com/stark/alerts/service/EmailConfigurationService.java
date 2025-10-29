package com.stark.alerts.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Servicio para configuración dinámica de emails
 */
@Service
public class EmailConfigurationService {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private final Environment environment;
    
    // Configuración temporal (en memoria)
    private final Map<String, String> tempConfig = new ConcurrentHashMap<>();
    
    @Autowired
    public EmailConfigurationService(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * Obtiene el email de origen (temporal o por defecto)
     */
    public String getEmailFrom() {
        return tempConfig.getOrDefault("emailFrom", 
            environment.getProperty("app.alerts.email-from", "noreply@stark.com"));
    }
    
    /**
     * Obtiene el email de destino (temporal o por defecto)
     */
    public String getEmailTo() {
        return tempConfig.getOrDefault("emailTo", 
            environment.getProperty("app.alerts.email-to", "admin@stark.com"));
    }
    
    /**
     * Configura emails temporalmente
     */
    public boolean setTemporaryEmails(String emailFrom, String emailTo) {
        if (!isValidEmail(emailFrom) || !isValidEmail(emailTo)) {
            return false;
        }
        
        tempConfig.put("emailFrom", emailFrom);
        tempConfig.put("emailTo", emailTo);
        
        return true;
    }
    
    /**
     * Guarda configuración permanentemente (actualiza application.yml)
     */
    public boolean savePermanentEmails(String emailFrom, String emailTo) {
        if (!isValidEmail(emailFrom) || !isValidEmail(emailTo)) {
            return false;
        }
        
        // Actualizar configuración temporal
        tempConfig.put("emailFrom", emailFrom);
        tempConfig.put("emailTo", emailTo);
        
        // TODO: Implementar guardado permanente en application.yml
        // Por ahora solo actualizamos la configuración temporal
        System.out.println("Configuración guardada temporalmente:");
        System.out.println("Email From: " + emailFrom);
        System.out.println("Email To: " + emailTo);
        
        return true;
    }
    
    /**
     * Resetea configuración temporal
     */
    public void resetTemporaryConfig() {
        tempConfig.clear();
    }
    
    /**
     * Obtiene configuración actual
     */
    public Map<String, String> getCurrentConfig() {
        Map<String, String> config = new ConcurrentHashMap<>();
        config.put("emailFrom", getEmailFrom());
        config.put("emailTo", getEmailTo());
        config.put("isTemporary", !tempConfig.isEmpty() ? "true" : "false");
        return config;
    }
    
    /**
     * Valida formato de email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}

