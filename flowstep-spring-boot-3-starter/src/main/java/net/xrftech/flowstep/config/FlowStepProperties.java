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
}