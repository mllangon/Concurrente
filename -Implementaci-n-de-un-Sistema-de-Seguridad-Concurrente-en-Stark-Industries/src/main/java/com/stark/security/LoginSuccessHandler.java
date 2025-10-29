package com.stark.security;

import com.stark.access.domain.AccessLog;
import com.stark.access.repo.AccessLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

/**
 * Manejador personalizado para registrar inicios de sesión exitosos como logs de acceso
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final AccessLogRepository accessLogRepository;
    
    public LoginSuccessHandler(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        // Obtener información del usuario autenticado
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        
        // Obtener información de la solicitud
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = getClientIpAddress(request);
        
        // Crear log de acceso para el inicio de sesión
        AccessLog loginLog = new AccessLog();
        loginLog.setPersonId(username);
        loginLog.setPersonName(username + " (" + role + ")");
        
        // Asignar ubicación según el rol
        String location = getLocationByRole(role, remoteAddr);
        loginLog.setLocation(location);
        
        loginLog.setAuthorized(true);
        loginLog.setTs(Instant.now());
        
        // Guardar el log de acceso
        accessLogRepository.save(loginLog);
        
        // Continuar con el flujo normal de redirección
        response.sendRedirect("/");
    }
    
    /**
     * Obtiene la ubicación según el rol del usuario
     */
    private String getLocationByRole(String role, String remoteAddr) {
        // Normalizar IP localhost
        String normalizedIp = normalizeLocalhostIp(remoteAddr);
        
        switch (role) {
            case "ROLE_ADMIN":
                return "Despacho Principal - " + normalizedIp;
            case "ROLE_SECURITY_ENGINEER":
                return "Centro de Seguridad - " + normalizedIp;
            case "ROLE_OPERATOR":
                return "Sala de Operaciones - " + normalizedIp;
            default:
                return "Sistema de Login - " + normalizedIp;
        }
    }
    
    /**
     * Normaliza las IPs de localhost a formato legible
     */
    private String normalizeLocalhostIp(String ip) {
        if (ip == null) return "localhost";
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "localhost";
        }
        if (ip.equals("127.0.0.1")) {
            return "localhost";
        }
        return ip;
    }
    
    /**
     * Obtiene la IP real del cliente considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
