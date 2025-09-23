package net.xrftech.flowstep.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark query services in the system.
 * Used for architectural governance and service identification.
 * 
 * Services annotated with @QueryFlow should:
 * - Inherit from QueryTemplate
 * - Be read-only operations (no side effects)
 * - Not require transactions
 * - Return pure business objects
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryFlow {
    /**
     * Unique code identifying this query service
     * @return service code
     */
    String code();
    
    /**
     * Human-readable description of this query service
     * @return service description
     */
    String desc();
    
    /**
     * Enable detailed logging for this query service.
     * When enabled, logs:
     * - Service execution start/end with timing
     * - Input parameters (sanitized for security)
     * - Step-by-step execution details
     * - Performance metrics
     * - Error details with context
     * - Response metadata (without sensitive data)
     * 
     * @return true to enable detailed logging, false otherwise
     */
    boolean enableLogging() default false;
    
    /**
     * Log level for this service when logging is enabled.
     * Allows fine-grained control over logging verbosity.
     * 
     * @return the log level to use
     */
    LogLevel logLevel() default LogLevel.INFO;
    
    /**
     * Include request/response data in logs.
     * When enabled, includes sanitized request and response data.
     * Sensitive fields are automatically masked.
     * 
     * @return true to include request/response data, false otherwise
     */
    boolean includeRequestResponse() default true;
    
    /**
     * Include performance metrics in logs.
     * When enabled, logs execution time, memory usage, and other metrics.
     * 
     * @return true to include performance metrics, false otherwise
     */
    boolean includePerformanceMetrics() default true;
    
    /**
     * Custom tags to add to log entries for this service.
     * Useful for filtering and searching logs.
     * 
     * @return array of custom tags
     */
    String[] tags() default {};
    
    /**
     * Log level enumeration for QueryFlow services
     */
    enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}