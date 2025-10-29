package com.stark.common.service;

import org.springframework.stereotype.Service;

/**
 * Servicio para generar mensajes de error profesionales y claros
 */
@Service
public class ErrorMessageService {
    
    /**
     * Genera mensaje de error para acceso denegado por rol
     */
    public String getAccessDeniedMessage(String action, String currentRole) {
        return String.format("Acceso denegado: Su rol '%s' no tiene permisos para %s. " +
                "Contacte al administrador del sistema si considera que necesita acceso a esta funcionalidad.", 
                formatRoleName(currentRole), action);
    }
    
    /**
     * Genera mensaje de error para operación no autorizada
     */
    public String getUnauthorizedMessage(String operation) {
        return String.format("Operación no autorizada: No tiene permisos para realizar la operación '%s'. " +
                "Verifique sus credenciales y permisos de acceso.", operation);
    }
    
    /**
     * Genera mensaje de error para recurso no encontrado
     */
    public String getResourceNotFoundMessage(String resource) {
        return String.format("Recurso no encontrado: El %s solicitado no existe o no está disponible. " +
                "Verifique el identificador y vuelva a intentar.", resource);
    }
    
    /**
     * Genera mensaje de error para validación fallida
     */
    public String getValidationErrorMessage(String field) {
        return String.format("Error de validación: El campo '%s' contiene información inválida. " +
                "Revise los datos ingresados y corrija los errores antes de continuar.", field);
    }
    
    /**
     * Genera mensaje de error para operación fallida
     */
    public String getOperationFailedMessage(String operation) {
        return String.format("Operación fallida: No se pudo completar la operación '%s'. " +
                "Intente nuevamente o contacte al soporte técnico si el problema persiste.", operation);
    }
    
    /**
     * Genera mensaje de error para sesión expirada
     */
    public String getSessionExpiredMessage() {
        return "Sesión expirada: Su sesión ha caducado por inactividad. " +
                "Por favor, inicie sesión nuevamente para continuar.";
    }
    
    /**
     * Genera mensaje de error para credenciales inválidas
     */
    public String getInvalidCredentialsMessage() {
        return "Credenciales inválidas: El nombre de usuario o contraseña son incorrectos. " +
                "Verifique sus datos e intente nuevamente.";
    }
    
    /**
     * Genera mensaje de error para servidor no disponible
     */
    public String getServerUnavailableMessage() {
        return "Servidor no disponible: El sistema está temporalmente fuera de servicio. " +
                "Intente nuevamente en unos minutos o contacte al administrador.";
    }
    
    /**
     * Genera mensaje de error para datos duplicados
     */
    public String getDuplicateDataMessage(String resource) {
        return String.format("Datos duplicados: Ya existe un %s con los mismos datos. " +
                "Verifique la información y utilice datos únicos.", resource);
    }
    
    /**
     * Genera mensaje de error para límite de intentos excedido
     */
    public String getTooManyAttemptsMessage() {
        return "Límite de intentos excedido: Ha realizado demasiados intentos fallidos. " +
                "Espere unos minutos antes de intentar nuevamente.";
    }
    
    /**
     * Formatea el nombre del rol para mostrar
     */
    private String formatRoleName(String role) {
        if (role == null) return "Desconocido";
        
        switch (role) {
            case "ROLE_ADMIN":
                return "Administrador";
            case "ROLE_SECURITY_ENGINEER":
                return "Ingeniero de Seguridad";
            case "ROLE_OPERATOR":
                return "Operador";
            default:
                return role.replace("ROLE_", "").replace("_", " ");
        }
    }
    
    /**
     * Genera mensaje de error genérico con contexto
     */
    public String getGenericErrorMessage(String context) {
        return String.format("Error del sistema: %s. " +
                "Si el problema persiste, contacte al administrador del sistema.", context);
    }
}
