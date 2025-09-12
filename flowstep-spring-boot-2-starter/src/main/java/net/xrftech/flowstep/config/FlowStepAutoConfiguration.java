package net.xrftech.flowstep.config;

import net.xrftech.flowstep.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for FlowStep Spring Boot 2 Starter
 * Automatically configures beans when the starter is included as a dependency
 */
@Configuration
@ConditionalOnProperty(name = "flowstep.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlowStepProperties.class)
public class FlowStepAutoConfiguration {

    /**
     * Auto-configures GlobalExceptionHandler if not already present
     * Only activates when Spring Web is on the classpath and exception handler is enabled
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    @ConditionalOnProperty(name = "flowstep.exception-handler.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public GlobalExceptionHandler flowStepGlobalExceptionHandler(FlowStepProperties properties) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        // Configure handler based on properties if needed
        return handler;
    }
}
