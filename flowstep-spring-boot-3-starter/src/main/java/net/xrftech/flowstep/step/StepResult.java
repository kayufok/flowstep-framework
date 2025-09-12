package net.xrftech.flowstep.step;

import net.xrftech.flowstep.exception.ErrorType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result wrapper for step execution outcomes.
 * 
 * Encapsulates the success/failure state, data, and error information
 * from step execution. This provides a consistent way to handle
 * step results throughout the template framework.
 * 
 * @param <T> the type of data returned by the step
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StepResult<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;
    private final ErrorType errorType;


    /**
     * Creates a successful step result with data.
     * 
     * @param data the result data
     * @param <T> the type of the data
     * @return a successful StepResult
     */
    public static <T> StepResult<T> success(T data) {
        return new StepResult<>(true, data, null, null, null);
    }

    /**
     * Creates a successful step result without data.
     * 
     * @param <T> the type of the data
     * @return a successful StepResult
     */
    public static <T> StepResult<T> success() {
        return success(null);
    }

    /**
     * Creates a failed step result with detailed error information.
     * 
     * @param message the error message
     * @param errorCode the error code
     * @param errorType the error type classification
     * @param <T> the type of the data
     * @return a failed StepResult
     */
    public static <T> StepResult<T> failure(String message, String errorCode, ErrorType errorType) {
        return new StepResult<>(false, null, message, errorCode, errorType);
    }

    /**
     * Creates a failed step result with just a message (for quick prototyping).
     * Uses default error code and BUSINESS error type.
     * 
     * @param message the error message
     * @param <T> the type of the data
     * @return a failed StepResult
     */
    public static <T> StepResult<T> failure(String message) {
        return failure(message, "GENERIC_ERROR", ErrorType.BUSINESS);
    }

    /**
     * Creates a validation failure result.
     * 
     * @param message the validation error message
     * @param <T> the type of the data
     * @return a failed StepResult with VALIDATION error type
     */
    public static <T> StepResult<T> validationFailure(String message) {
        return failure(message, "VALIDATION_ERROR", ErrorType.VALIDATION);
    }

    /**
     * Creates a system failure result.
     * 
     * @param message the system error message
     * @param <T> the type of the data
     * @return a failed StepResult with SYSTEM error type
     */
    public static <T> StepResult<T> systemFailure(String message) {
        return failure(message, "SYSTEM_ERROR", ErrorType.SYSTEM);
    }

    /**
     * Checks if this result represents a failed operation.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return !success;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("StepResult{success=true, data=%s}", data);
        } else {
            return String.format("StepResult{success=false, message='%s', errorCode='%s', errorType=%s}", 
                    message, errorCode, errorType);
        }
    }
}
