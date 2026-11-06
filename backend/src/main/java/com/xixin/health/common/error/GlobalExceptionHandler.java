package com.xixin.health.common.error;

import com.xixin.health.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private <T> ResponseEntity<ApiResponse<T>> json(HttpStatus status, ApiResponse<T> body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case AUTH_INVALID_CREDENTIALS, AUTH_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case AUTH_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.CONFLICT;
        };
        return json(status, ApiResponse.failure(exception.getErrorCode().code(), exception.getMessage()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        return json(HttpStatus.BAD_REQUEST, ApiResponse.failure(ErrorCode.VALIDATION_ERROR.code(), exception.getMessage()));
    }

    @ExceptionHandler({
        AccessDeniedException.class,
        AuthorizationDeniedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(Exception exception) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.failure(ErrorCode.AUTH_FORBIDDEN.code(), ErrorCode.AUTH_FORBIDDEN.defaultMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        return json(HttpStatus.INTERNAL_SERVER_ERROR, ApiResponse.failure(ErrorCode.SYSTEM_ERROR.code(), exception.getMessage()));
    }
}
