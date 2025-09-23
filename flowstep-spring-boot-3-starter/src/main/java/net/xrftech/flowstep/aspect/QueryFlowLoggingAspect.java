package net.xrftech.flowstep.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.annotation.QueryFlow;
import net.xrftech.flowstep.config.FlowStepProperties;
import net.xrftech.flowstep.logging.FlowStepLoggingService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP Aspect for intercepting QueryFlow annotated services to provide
 * comprehensive logging capabilities.
 * 
 * This aspect intercepts calls to the execute() method of services
 * annotated with @QueryFlow and provides:
 * - Execution timing
 * - Request/response logging (with sanitization)
 * - Error handling and logging
 * - Performance metrics
 * - Structured logging with context
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute before other aspects
@ConditionalOnProperty(name = "flowstep.logging.enabled", havingValue = "true", matchIfMissing = false)
public class QueryFlowLoggingAspect {
    
    private final FlowStepLoggingService loggingService;
    private final FlowStepProperties properties;
    
    /**
     * Intercept execution of QueryFlow annotated services
     */
    @Around("@target(queryFlow) && execution(* execute(..))")
    public Object aroundQueryFlowExecution(ProceedingJoinPoint joinPoint, QueryFlow queryFlow) throws Throwable {
        
        // Skip logging if not enabled for this service
        if (!queryFlow.enableLogging() && !properties.getLogging().isForceLoggingEnabled()) {
            return joinPoint.proceed();
        }
        
        String serviceCode = queryFlow.code();
        String serviceDesc = queryFlow.desc();
        QueryFlow.LogLevel logLevel = queryFlow.logLevel();
        String[] tags = queryFlow.tags();
        
        Object[] args = joinPoint.getArgs();
        Object request = args.length > 0 ? args[0] : null;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log service start
            loggingService.logServiceStart(serviceCode, serviceDesc, request, logLevel, tags);
            
            // Execute the actual service method
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log successful completion
            loggingService.logServiceEnd(serviceCode, result, executionTime, logLevel, 
                                       queryFlow.includeRequestResponse(), queryFlow.includePerformanceMetrics());
            
            return result;
            
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log error
            loggingService.logServiceError(serviceCode, throwable, executionTime, logLevel, request);
            
            // Re-throw the exception to maintain normal error handling
            throw throwable;
        }
    }
    
    /**
     * Intercept step executions within QueryFlow services for detailed logging
     */
    @Around("@target(queryFlow) && execution(* net.xrftech.flowstep.step.QueryStep+.execute(..))")
    public Object aroundStepExecution(ProceedingJoinPoint joinPoint, QueryFlow queryFlow) throws Throwable {
        
        // Skip logging if not enabled for this service
        if (!queryFlow.enableLogging() && !properties.getLogging().isForceLoggingEnabled()) {
            return joinPoint.proceed();
        }
        
        String serviceCode = queryFlow.code();
        QueryFlow.LogLevel logLevel = queryFlow.logLevel();
        String stepName = joinPoint.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log step start
            loggingService.logStepExecution(serviceCode, stepName, "START", 0, null, logLevel);
            
            // Execute the step
            Object result = joinPoint.proceed();
            
            long stepTime = System.currentTimeMillis() - startTime;
            
            // Log step completion
            loggingService.logStepExecution(serviceCode, stepName, "COMPLETE", stepTime, result, logLevel);
            
            return result;
            
        } catch (Throwable throwable) {
            long stepTime = System.currentTimeMillis() - startTime;
            
            // Log step error
            loggingService.logStepExecution(serviceCode, stepName, "ERROR", stepTime, 
                                          throwable.getMessage(), logLevel);
            
            throw throwable;
        }
    }
    
    /**
     * Helper method to get QueryFlow annotation from the target class
     */
    private QueryFlow getQueryFlowAnnotation(ProceedingJoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        QueryFlow annotation = targetClass.getAnnotation(QueryFlow.class);
        
        if (annotation == null) {
            // Check for annotation on interfaces
            for (Class<?> interfaceClass : targetClass.getInterfaces()) {
                annotation = interfaceClass.getAnnotation(QueryFlow.class);
                if (annotation != null) {
                    break;
                }
            }
        }
        
        return annotation;
    }
}