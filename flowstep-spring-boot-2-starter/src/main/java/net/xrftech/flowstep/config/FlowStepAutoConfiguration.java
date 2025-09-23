package net.xrftech.flowstep.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.xrftech.flowstep.aspect.CommandFlowLoggingAspect;
import net.xrftech.flowstep.aspect.QueryFlowLoggingAspect;
import net.xrftech.flowstep.exception.GlobalExceptionHandler;
import net.xrftech.flowstep.logging.FlowStepLoggingService;
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
    
    /**
     * Auto-configures ObjectMapper for FlowStep logging if not already present
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper flowStepObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    /**
     * Auto-configures FlowStepLoggingService for structured logging
     */
    @Bean
    @ConditionalOnProperty(name = "flowstep.logging.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public FlowStepLoggingService flowStepLoggingService(ObjectMapper objectMapper) {
        return new FlowStepLoggingService(objectMapper);
    }
    
    /**
     * Auto-configures QueryFlowLoggingAspect for AOP-based logging
     * Only activates when AspectJ is on the classpath and logging is enabled
     */
    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnProperty(name = "flowstep.logging.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public QueryFlowLoggingAspect queryFlowLoggingAspect(
            FlowStepLoggingService loggingService, 
            FlowStepProperties properties) {
        return new QueryFlowLoggingAspect(loggingService, properties);
    }
    
    /**
     * Auto-configures CommandFlowLoggingAspect for AOP-based logging
     * Only activates when AspectJ is on the classpath and logging is enabled
     */
    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnProperty(name = "flowstep.logging.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public CommandFlowLoggingAspect commandFlowLoggingAspect(
            FlowStepLoggingService loggingService, 
            FlowStepProperties properties) {
        return new CommandFlowLoggingAspect(loggingService, properties);
    }
}
