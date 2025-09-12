package net.xrftech.flowstep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for FlowStep Framework
 * Handles BusinessException and other common exceptions using modern Java features
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        var errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
        
        var httpStatus = switch (ex.getErrorType()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS -> HttpStatus.CONFLICT;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        var errorMessage = fieldError != null 
            ? "Validation failed for field '%s': %s".formatted(fieldError.getField(), fieldError.getDefaultMessage())
            : "Validation failed";
            
        var errorResponse = new ErrorResponse("VALIDATION_ERROR", errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        var errorMessage = fieldError != null
            ? "Binding failed for field '%s': %s".formatted(fieldError.getField(), fieldError.getDefaultMessage())
            : "Binding failed";
            
        var errorResponse = new ErrorResponse("BINDING_ERROR", errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        var errorMessage = "Invalid value '%s' for parameter '%s'. Expected type: %s"
            .formatted(ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
            
        var errorResponse = new ErrorResponse("TYPE_MISMATCH_ERROR", errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        var errorResponse = new ErrorResponse("ILLEGAL_ARGUMENT", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        var errorResponse = new ErrorResponse("INTERNAL_ERROR", "An internal error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
