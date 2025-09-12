package net.xrftech.flowstep.exception;

import lombok.Getter;

/**
 * Business exception model for handling expected business errors.
 * 
 * This exception is used throughout the template framework to represent
 * business logic failures that should be handled gracefully and returned
 * to the client with appropriate error information.
 */
@Getter
public class BusinessException extends Exception {
    private final String errorCode;
    private final ErrorType errorType;

    /**
     * Creates a new BusinessException with error code, message, and type.
     * 
     * @param errorCode unique error code for this exception
     * @param message human-readable error message
     * @param errorType classification of this error
     */
    public BusinessException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    /**
     * Convenience constructor for BUSINESS type errors.
     * 
     * @param errorCode unique error code for this exception
     * @param message human-readable error message
     */
    public BusinessException(String errorCode, String message) {
        this(errorCode, message, ErrorType.BUSINESS);
    }


    @Override
    public String toString() {
        return String.format("BusinessException{errorCode='%s', errorType=%s, message='%s'}", 
                errorCode, errorType, getMessage());
    }
}
