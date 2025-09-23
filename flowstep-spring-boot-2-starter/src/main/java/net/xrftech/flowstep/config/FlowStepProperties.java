package net.xrftech.flowstep.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FlowStep Spring Boot Starter
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
    private ExceptionHandler exceptionHandler = new ExceptionHandler();
    
    /**
     * Performance monitoring configuration
     */
    private PerformanceConfig performance = new PerformanceConfig();
    
    /**
     * Logging configuration
     */
    private LoggingConfig logging = new LoggingConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    public PerformanceConfig getPerformance() {
        return performance;
    }
    
    public void setPerformance(PerformanceConfig performance) {
        this.performance = performance;
    }
    
    public LoggingConfig getLogging() {
        return logging;
    }
    
    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }

    public static class ExceptionHandler {
        /**
         * Whether to enable the global exception handler. Default is true.
         */
        private boolean enabled = true;

        /**
         * Whether to include stack traces in error responses. Default is false.
         */
        private boolean includeStackTrace = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeStackTrace() {
            return includeStackTrace;
        }

        public void setIncludeStackTrace(boolean includeStackTrace) {
            this.includeStackTrace = includeStackTrace;
        }
    }
    
    /**
     * Performance monitoring configuration
     */
    public static class PerformanceConfig {
        private boolean enabled = true;
        private boolean logSlowQueries = true;
        private long slowQueryThresholdMs = 1000L;
        private boolean enableMetrics = false;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isLogSlowQueries() {
            return logSlowQueries;
        }
        
        public void setLogSlowQueries(boolean logSlowQueries) {
            this.logSlowQueries = logSlowQueries;
        }
        
        public long getSlowQueryThresholdMs() {
            return slowQueryThresholdMs;
        }
        
        public void setSlowQueryThresholdMs(long slowQueryThresholdMs) {
            if (slowQueryThresholdMs < 0) {
                throw new IllegalArgumentException("Slow query threshold must be non-negative");
            }
            this.slowQueryThresholdMs = slowQueryThresholdMs;
        }
        
        public boolean isEnableMetrics() {
            return enableMetrics;
        }
        
        public void setEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
        }
    }
    
    /**
     * Logging configuration
     */
    public static class LoggingConfig {
        private boolean enabled = false;
        private boolean forceLoggingEnabled = false;
        private boolean includeStackTraces = false;
        private int maxRequestResponseSize = 10000;
        private String[] sensitiveFieldPatterns = new String[0];
        private String defaultLogLevel = "INFO";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isForceLoggingEnabled() {
            return forceLoggingEnabled;
        }
        
        public void setForceLoggingEnabled(boolean forceLoggingEnabled) {
            this.forceLoggingEnabled = forceLoggingEnabled;
        }
        
        public boolean isIncludeStackTraces() {
            return includeStackTraces;
        }
        
        public void setIncludeStackTraces(boolean includeStackTraces) {
            this.includeStackTraces = includeStackTraces;
        }
        
        public int getMaxRequestResponseSize() {
            return maxRequestResponseSize;
        }
        
        public void setMaxRequestResponseSize(int maxRequestResponseSize) {
            if (maxRequestResponseSize < 0) {
                throw new IllegalArgumentException("Max request/response size must be non-negative");
            }
            this.maxRequestResponseSize = maxRequestResponseSize;
        }
        
        public String[] getSensitiveFieldPatterns() {
            return sensitiveFieldPatterns;
        }
        
        public void setSensitiveFieldPatterns(String[] sensitiveFieldPatterns) {
            this.sensitiveFieldPatterns = sensitiveFieldPatterns;
        }
        
        public String getDefaultLogLevel() {
            return defaultLogLevel;
        }
        
        public void setDefaultLogLevel(String defaultLogLevel) {
            if (defaultLogLevel == null || defaultLogLevel.trim().isEmpty()) {
                throw new IllegalArgumentException("Default log level cannot be null or empty");
            }
            
            // Validate log level
            switch (defaultLogLevel.toUpperCase()) {
                case "TRACE":
                case "DEBUG":
                case "INFO":
                case "WARN":
                case "ERROR":
                    break;
                default:
                    throw new IllegalArgumentException("Invalid default log level: " + defaultLogLevel);
            }
            
            this.defaultLogLevel = defaultLogLevel;
        }
    }
}