package net.xrftech.flowstep.exception;

/**
 * Error type enum for unified error nature identification.
 * Applicable to StepResult and BusinessException.
 * 
 * This enum helps categorize errors for proper handling:
 * - HTTP status code mapping
 * - Monitoring and alerting
 * - Frontend error display
 */
public enum ErrorType {
    /**
     * Input validation error (e.g., missing parameters, format errors)
     * Usually fixed by client adjusting the request.
     * Maps to HTTP 400 Bad Request.
     */
    VALIDATION,

    /**
     * Business logic error (e.g., insufficient balance, out of stock)
     * Usually resolved by business rules or user decisions.
     * Maps to HTTP 409 Conflict.
     */
    BUSINESS,

    /**
     * System internal error (e.g., database connection failure, null pointer)
     * Requires developer intervention to fix.
     * Maps to HTTP 500 Internal Server Error.
     */
    SYSTEM
}
