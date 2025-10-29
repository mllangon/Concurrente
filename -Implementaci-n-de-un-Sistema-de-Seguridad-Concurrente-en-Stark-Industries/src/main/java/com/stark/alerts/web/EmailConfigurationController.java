package com.stark.alerts.web;

import com.stark.alerts.service.EmailConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para configuración de emails
 */
@RestController
@RequestMapping("/api/email-config")
public class EmailConfigurationController {
    
    private final EmailConfigurationService emailConfigService;
    
    public EmailConfigurationController(EmailConfigurationService emailConfigService) {
        this.emailConfigService = emailConfigService;
    }
    
    /**
     * Obtiene la configuración actual de emails
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, String>> getCurrentConfig() {
        return ResponseEntity.ok(emailConfigService.getCurrentConfig());
    }
    
    /**
     * Configura emails temporalmente
     */
    @PostMapping("/set-temporary")
    public ResponseEntity<Map<String, Object>> setTemporaryEmails(
            @RequestParam String emailFrom,
            @RequestParam String emailTo) {
        
        boolean success = emailConfigService.setTemporaryEmails(emailFrom, emailTo);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuración temporal aplicada correctamente",
                "emailFrom", emailFrom,
                "emailTo", emailTo
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Formato de email inválido"
            ));
        }
    }
    
    /**
     * Guarda configuración permanentemente
     */
    @PostMapping("/save-permanent")
    public ResponseEntity<Map<String, Object>> savePermanentEmails(
            @RequestParam String emailFrom,
            @RequestParam String emailTo) {
        
        boolean success = emailConfigService.savePermanentEmails(emailFrom, emailTo);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuración guardada permanentemente",
                "emailFrom", emailFrom,
                "emailTo", emailTo
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Formato de email inválido"
            ));
        }
    }
    
    /**
     * Resetea configuración temporal
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetConfig() {
        emailConfigService.resetTemporaryConfig();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Configuración temporal reseteada",
            "config", emailConfigService.getCurrentConfig()
        ));
    }
}
