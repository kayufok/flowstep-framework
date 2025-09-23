package net.xrftech.flowstep.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.annotation.CommandFlow;
import net.xrftech.flowstep.config.FlowStepProperties;
import net.xrftech.flowstep.logging.FlowStepLoggingService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for intercepting CommandFlow annotated services to provide
 * comprehensive logging capabilities for command operations.
 * 
 * This aspect intercepts calls to the execute() method of services
 * annotated with @CommandFlow and provides:
 * - Execution timing
 * - Request/response logging (with sanitization)
 * - Error handling and logging
 * - Performance metrics
 * - Audit information logging
 * - Transaction boundary logging
 * - Structured logging with context
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute before other aspects
@ConditionalOnProperty(name = "flowstep.logging.enabled", havingValue = "true", matchIfMissing = false)
public class CommandFlowLoggingAspect {
    
    private final FlowStepLoggingService loggingService;
    private final FlowStepProperties properties;
    
    /**
     * Intercept execution of CommandFlow annotated services
     */
    @Around("@target(commandFlow) && execution(* execute(..))")
    public Object aroundCommandFlowExecution(ProceedingJoinPoint joinPoint, CommandFlow commandFlow) throws Throwable {
        
        // Skip logging if not enabled for this service
        if (!commandFlow.enableLogging() && !properties.getLogging().isForceLoggingEnabled()) {
            return joinPoint.proceed();
        }
        
        String serviceCode = commandFlow.code();
        String serviceDesc = commandFlow.desc();
        CommandFlow.LogLevel logLevel = commandFlow.logLevel();
        String[] tags = commandFlow.tags();
        
        Object[] args = joinPoint.getArgs();
        Object request = args.length > 0 ? args[0] : null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log service start (convert CommandFlow.LogLevel to QueryFlow.LogLevel for compatibility)
            loggingService.logServiceStart(serviceCode, serviceDesc, request, 
                                         convertLogLevel(logLevel), tags);
            
            // Log transaction start if audit info is enabled
            if (commandFlow.includeAuditInfo()) {
                loggingService.logCustomMessage(serviceCode, "Transaction started", 
                                              "Command execution within transactional boundary", 
                                              convertLogLevel(logLevel));
            }
            
            // Execute the actual service method
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log transaction completion if audit info is enabled
            if (commandFlow.includeAuditInfo()) {
                loggingService.logCustomMessage(serviceCode, "Transaction completed successfully", 
                                              String.format("Command execution completed, duration: %dms", executionTime), 
                                              convertLogLevel(logLevel));
            }
            
            // Log successful completion
            loggingService.logServiceEnd(serviceCode, result, executionTime, convertLogLevel(logLevel), 
                                       commandFlow.includeRequestResponse(), commandFlow.includePerformanceMetrics());
            
            return result;
            
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log transaction rollback if audit info is enabled
            if (commandFlow.includeAuditInfo()) {
                loggingService.logCustomMessage(serviceCode, "Transaction rolled back", 
                                              String.format("Command execution failed, transaction rolled back after %dms", executionTime), 
                                              convertLogLevel(logLevel));
            }
            
            // Log error
            loggingService.logServiceError(serviceCode, throwable, executionTime, 
                                         convertLogLevel(logLevel), request);
            
            // Re-throw the exception to maintain normal error handling
            throw throwable;
        }
    }
    
    /**
     * Intercept step executions within CommandFlow services for detailed logging
     */
    @Around("@target(commandFlow) && execution(* net.xrftech.flowstep.step.CommandStep+.execute(..))")
    public Object aroundStepExecution(ProceedingJoinPoint joinPoint, CommandFlow commandFlow) throws Throwable {
        
        // Skip logging if not enabled for this service
        if (!commandFlow.enableLogging() && !properties.getLogging().isForceLoggingEnabled()) {
            return joinPoint.proceed();
        }
        
        String serviceCode = commandFlow.code();
        CommandFlow.LogLevel logLevel = commandFlow.logLevel();
        String stepName = joinPoint.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log step start
            loggingService.logStepExecution(serviceCode, stepName, "START", 0, null, convertLogLevel(logLevel));
            
            // Execute the step
            Object result = joinPoint.proceed();
            
            long stepTime = System.currentTimeMillis() - startTime;
            
            // Log step completion with audit info if enabled
            if (commandFlow.includeAuditInfo()) {
                loggingService.logStepExecution(serviceCode, stepName, "COMPLETE", stepTime, 
                                              String.format("Step completed successfully in %dms", stepTime), 
                                              convertLogLevel(logLevel));
            } else {
                loggingService.logStepExecution(serviceCode, stepName, "COMPLETE", stepTime, result, convertLogLevel(logLevel));
            }
            
            return result;
            
        } catch (Throwable throwable) {
            long stepTime = System.currentTimeMillis() - startTime;
            
            // Log step error with audit context
            String errorContext = commandFlow.includeAuditInfo() ? 
                String.format("Step failed after %dms, error: %s", stepTime, throwable.getMessage()) :
                throwable.getMessage();
                
            loggingService.logStepExecution(serviceCode, stepName, "ERROR", stepTime, 
                                          errorContext, convertLogLevel(logLevel));
            
            throw throwable;
        }
    }
    
    /**
     * Intercept post-execution handling for audit logging
     */
    @Around("@target(commandFlow) && execution(* handlePostExecution(..))")
    public Object aroundPostExecution(ProceedingJoinPoint joinPoint, CommandFlow commandFlow) throws Throwable {
        
        // Skip logging if not enabled for this service or audit info is disabled
        if ((!commandFlow.enableLogging() && !properties.getLogging().isForceLoggingEnabled()) 
            || !commandFlow.includeAuditInfo()) {
            return joinPoint.proceed();
        }
        
        String serviceCode = commandFlow.code();
        CommandFlow.LogLevel logLevel = commandFlow.logLevel();
        
        try {
            // Log post-execution start
            loggingService.logCustomMessage(serviceCode, "Post-execution handling started", 
                                          "Processing events and audit information", 
                                          convertLogLevel(logLevel));
            
            // Execute post-execution handling
            Object result = joinPoint.proceed();
            
            // Log post-execution completion
            loggingService.logCustomMessage(serviceCode, "Post-execution handling completed", 
                                          "Events published and audit information recorded", 
                                          convertLogLevel(logLevel));
            
            return result;
            
        } catch (Throwable throwable) {
            // Log post-execution error
            loggingService.logCustomMessage(serviceCode, "Post-execution handling failed", 
                                          "Error in event publishing or audit recording: " + throwable.getMessage(), 
                                          convertLogLevel(logLevel));
            
            throw throwable;
        }
    }
    
    /**
     * Convert CommandFlow.LogLevel to QueryFlow.LogLevel for compatibility with logging service
     */
    private net.xrftech.flowstep.annotation.QueryFlow.LogLevel convertLogLevel(CommandFlow.LogLevel commandLogLevel) {
        return switch (commandLogLevel) {
            case TRACE -> net.xrftech.flowstep.annotation.QueryFlow.LogLevel.TRACE;
            case DEBUG -> net.xrftech.flowstep.annotation.QueryFlow.LogLevel.DEBUG;
            case INFO -> net.xrftech.flowstep.annotation.QueryFlow.LogLevel.INFO;
            case WARN -> net.xrftech.flowstep.annotation.QueryFlow.LogLevel.WARN;
            case ERROR -> net.xrftech.flowstep.annotation.QueryFlow.LogLevel.ERROR;
        };
    }
}