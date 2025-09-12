package net.xrftech.flowstep.exception;

/**
 * Standard error response structure for FlowStep Framework
 * 
 * This record provides immutable error response data with built-in
 * equals, hashCode, and toString implementations.
 */
public record ErrorResponse(
    String errorCode,
    String errorMessage
) {
    
    /**
     * Creates an ErrorResponse with validation for required fields.
     * 
     * @param errorCode the error code (must not be null or blank)
     * @param errorMessage the error message (must not be null or blank)
     */
    public ErrorResponse {
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalArgumentException("Error code cannot be null or blank");
        }
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("Error message cannot be null or blank");
        }
    }
    
    /**
     * Factory method for creating ErrorResponse instances.
     * 
     * @param errorCode the error code
     * @param errorMessage the error message
     * @return a new ErrorResponse instance
     */
    public static ErrorResponse of(String errorCode, String errorMessage) {
        return new ErrorResponse(errorCode, errorMessage);
    }
}
