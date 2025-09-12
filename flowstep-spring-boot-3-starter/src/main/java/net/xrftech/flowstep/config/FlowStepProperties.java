package net.xrftech.flowstep.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FlowStep Spring Boot Starter
 * 
 * Uses modern Java records for nested configuration to provide
 * immutable, thread-safe configuration objects.
 */
@ConfigurationProperties(prefix = "flowstep")
public class FlowStepProperties {

    /**
     * Whether FlowStep is enabled. Default is true.
     */
    private boolean enabled = true;

    /**
     * Exception handler configuration
     */
    private ExceptionHandlerConfig exceptionHandler = new ExceptionHandlerConfig();

    /**
     * Performance monitoring configuration
     */
    private PerformanceConfig performance = new PerformanceConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ExceptionHandlerConfig getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandlerConfig exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public PerformanceConfig getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }

    /**
     * Exception handler configuration record
     * 
     * @param enabled whether to enable the global exception handler
     * @param includeStackTrace whether to include stack traces in error responses
     * @param includeDebugInfo whether to include debug information in development mode
     */
    public record ExceptionHandlerConfig(
        boolean enabled,
        boolean includeStackTrace,
        boolean includeDebugInfo
    ) {
        public ExceptionHandlerConfig() {
            this(true, false, false);
        }
        
        public ExceptionHandlerConfig {
            // Validation: stack traces should only be enabled in debug mode
            if (includeStackTrace && !includeDebugInfo) {
                throw new IllegalArgumentException(
                    "Stack traces should only be enabled when debug info is also enabled"
                );
            }
        }
    }

    /**
     * Performance monitoring configuration record
     * 
     * @param enabled whether performance monitoring is enabled
     * @param logSlowQueries whether to log slow queries
     * @param slowQueryThresholdMs threshold in milliseconds for considering a query slow
     * @param enableMetrics whether to enable metrics collection
     */
    public record PerformanceConfig(
        boolean enabled,
        boolean logSlowQueries,
        long slowQueryThresholdMs,
        boolean enableMetrics
    ) {
        public PerformanceConfig() {
            this(true, true, 1000L, false);
        }
        
        public PerformanceConfig {
            if (slowQueryThresholdMs < 0) {
                throw new IllegalArgumentException("Slow query threshold must be non-negative");
            }
        }
    }
}