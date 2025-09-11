package net.xrftech.flowstep.config;

import net.xrftech.flowstep.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for FlowStep Framework
 * Automatically configures beans when the library is included as a dependency
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
public class FlowStepAutoConfiguration {

    /**
     * Auto-configures GlobalExceptionHandler if not already present
     * Only activates when Spring Web is on the classpath
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler flowStepGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
