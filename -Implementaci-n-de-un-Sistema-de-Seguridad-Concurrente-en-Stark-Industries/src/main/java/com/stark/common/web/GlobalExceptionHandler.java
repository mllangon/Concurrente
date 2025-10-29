package com.stark.common.web;

import com.stark.common.service.ErrorMessageService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorMessageService errorMessageService;

    public GlobalExceptionHandler(ErrorMessageService errorMessageService) {
        this.errorMessageService = errorMessageService;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        String currentRole = getCurrentUserRole();
        String action = extractActionFromException(ex);
        String message = errorMessageService.getAccessDeniedMessage(action, currentRole);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 403,
                        "error", "Forbidden",
                        "message", message,
                        "role", currentRole));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        String message = errorMessageService.getInvalidCredentialsMessage();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 401,
                        "error", "Unauthorized",
                        "message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        
        String message = errorMessageService.getValidationErrorMessage("datos del formulario");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", message,
                        "details", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        String message = errorMessageService.getOperationFailedMessage("operación solicitada");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        String message = errorMessageService.getGenericErrorMessage("Error interno del servidor");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", message));
    }

    private String getCurrentUserRole() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                return auth.getAuthorities().iterator().next().getAuthority();
            }
        } catch (Exception e) {
            // Ignore exceptions when getting role
        }
        return "UNKNOWN";
    }

    private String extractActionFromException(AccessDeniedException ex) {
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("create") || message.contains("crear")) {
                return "crear este recurso";
            } else if (message.contains("read") || message.contains("ver") || message.contains("list")) {
                return "ver este recurso";
            } else if (message.contains("update") || message.contains("actualizar") || message.contains("modificar")) {
                return "modificar este recurso";
            } else if (message.contains("delete") || message.contains("eliminar")) {
                return "eliminar este recurso";
            } else if (message.contains("access") || message.contains("acceso")) {
                return "acceder a esta funcionalidad";
            }
        }
        return "realizar esta operación";
    }
}




