package net.xrftech.flowstep.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.annotation.QueryFlow;
import net.xrftech.flowstep.annotation.CommandFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Centralized logging service for FlowStep operations.
 * Provides structured logging with context, sanitization, and performance metrics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowStepLoggingService {
    
    private final ObjectMapper objectMapper;
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final Map<String, Logger> loggerCache = new ConcurrentHashMap<>();
    
    // Sensitive field patterns for data sanitization
    private static final Set<Pattern> SENSITIVE_PATTERNS = Set.of(
        Pattern.compile(".*password.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*token.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*secret.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*key.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*auth.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*credential.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*ssn.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*social.*security.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*credit.*card.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*cvv.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*pin.*", Pattern.CASE_INSENSITIVE)
    );
    
    private static final String MASK_VALUE = "***MASKED***";
    
    /**
     * Log service execution start
     */
    public void logServiceStart(String serviceCode, String serviceDesc, Object request, 
                               QueryFlow.LogLevel logLevel, String[] tags) {
        Logger logger = getLogger(serviceCode);
        String executionId = generateExecutionId();
        
        // Set up MDC for structured logging
        setupMDC(serviceCode, serviceDesc, executionId, tags);
        
        LogEntry logEntry = LogEntry.builder()
            .timestamp(LocalDateTime.now())
            .executionId(executionId)
            .serviceCode(serviceCode)
            .serviceDescription(serviceDesc)
            .phase("START")
            .message("Service execution started")
            .request(sanitizeObject(request))
            .tags(Arrays.asList(tags))
            .build();
        
        logAtLevel(logger, logLevel, "🚀 [{}] {} - {}", serviceCode, serviceDesc, toJsonString(logEntry));
    }
    
    /**
     * Log service execution end
     */
    public void logServiceEnd(String serviceCode, Object response, long executionTimeMs, 
                             QueryFlow.LogLevel logLevel, boolean includeResponse, boolean includeMetrics) {
        Logger logger = getLogger(serviceCode);
        String executionId = MDC.get("executionId");
        
        LogEntry.LogEntryBuilder builder = LogEntry.builder()
            .timestamp(LocalDateTime.now())
            .executionId(executionId)
            .serviceCode(serviceCode)
            .phase("END")
            .message("Service execution completed successfully")
            .executionTimeMs(executionTimeMs);
        
        if (includeResponse && response != null) {
            builder.response(sanitizeObject(response));
        }
        
        if (includeMetrics) {
            builder.performanceMetrics(collectPerformanceMetrics(executionTimeMs));
        }
        
        LogEntry logEntry = builder.build();
        
        logAtLevel(logger, logLevel, "✅ [{}] Completed in {}ms - {}", 
                  serviceCode, executionTimeMs, toJsonString(logEntry));
        
        // Clean up MDC
        clearMDC();
    }
    
    /**
     * Log service execution error
     */
    public void logServiceError(String serviceCode, Throwable error, long executionTimeMs, 
                               QueryFlow.LogLevel logLevel, Object request) {
        Logger logger = getLogger(serviceCode);
        String executionId = MDC.get("executionId");
        
        LogEntry logEntry = LogEntry.builder()
            .timestamp(LocalDateTime.now())
            .executionId(executionId)
            .serviceCode(serviceCode)
            .phase("ERROR")
            .message("Service execution failed")
            .executionTimeMs(executionTimeMs)
            .error(ErrorInfo.from(error))
            .request(sanitizeObject(request))
            .build();
        
        logAtLevel(logger, logLevel, "❌ [{}] Failed after {}ms - {}", 
                  serviceCode, executionTimeMs, toJsonString(logEntry));
        
        // Clean up MDC
        clearMDC();
    }
    
    /**
     * Log step execution
     */
    public void logStepExecution(String serviceCode, String stepName, String phase, 
                                long stepTimeMs, Object stepData, QueryFlow.LogLevel logLevel) {
        Logger logger = getLogger(serviceCode);
        String executionId = MDC.get("executionId");
        
        LogEntry logEntry = LogEntry.builder()
            .timestamp(LocalDateTime.now())
            .executionId(executionId)
            .serviceCode(serviceCode)
            .stepName(stepName)
            .phase(phase)
            .message(String.format("Step %s: %s", stepName, phase))
            .executionTimeMs(stepTimeMs)
            .stepData(sanitizeObject(stepData))
            .build();
        
        String emoji = "START".equals(phase) ? "🔄" : "COMPLETE".equals(phase) ? "✓" : "⚠️";
        logAtLevel(logger, logLevel, "{} [{}] Step {} - {}", 
                  emoji, serviceCode, stepName, toJsonString(logEntry));
    }
    
    /**
     * Log custom message with context
     */
    public void logCustomMessage(String serviceCode, String message, Object data, 
                                QueryFlow.LogLevel logLevel) {
        Logger logger = getLogger(serviceCode);
        String executionId = MDC.get("executionId");
        
        LogEntry logEntry = LogEntry.builder()
            .timestamp(LocalDateTime.now())
            .executionId(executionId)
            .serviceCode(serviceCode)
            .phase("CUSTOM")
            .message(message)
            .stepData(sanitizeObject(data))
            .build();
        
        logAtLevel(logger, logLevel, "📝 [{}] {} - {}", serviceCode, message, toJsonString(logEntry));
    }
    
    // Private helper methods
    
    private Logger getLogger(String serviceCode) {
        return loggerCache.computeIfAbsent(serviceCode, 
            code -> LoggerFactory.getLogger("net.xrftech.flowstep.service." + code));
    }
    
    private String generateExecutionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void setupMDC(String serviceCode, String serviceDesc, String executionId, String[] tags) {
        MDC.put("serviceCode", serviceCode);
        MDC.put("serviceDescription", serviceDesc);
        MDC.put("executionId", executionId);
        MDC.put("tags", String.join(",", tags));
        MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    private void clearMDC() {
        MDC.clear();
    }
    
    private void logAtLevel(Logger logger, QueryFlow.LogLevel logLevel, String message, Object... args) {
        switch (logLevel) {
            case TRACE -> {
                if (logger.isTraceEnabled()) logger.trace(message, args);
            }
            case DEBUG -> {
                if (logger.isDebugEnabled()) logger.debug(message, args);
            }
            case INFO -> {
                if (logger.isInfoEnabled()) logger.info(message, args);
            }
            case WARN -> {
                if (logger.isWarnEnabled()) logger.warn(message, args);
            }
            case ERROR -> {
                if (logger.isErrorEnabled()) logger.error(message, args);
            }
        }
    }
    
    private Object sanitizeObject(Object obj) {
        if (obj == null) return null;
        
        try {
            // Convert to map for easier processing
            String json = objectMapper.writeValueAsString(obj);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return sanitizeMap(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to sanitize object: {}", e.getMessage());
            return obj.getClass().getSimpleName() + " (sanitization failed)";
        }
    }
    
    @SuppressWarnings("unchecked")
    private Object sanitizeMap(Map<String, Object> map) {
        Map<String, Object> sanitized = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (isSensitiveField(key)) {
                sanitized.put(key, MASK_VALUE);
            } else if (value instanceof Map) {
                sanitized.put(key, sanitizeMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                sanitized.put(key, sanitizeList((List<Object>) value));
            } else {
                sanitized.put(key, value);
            }
        }
        
        return sanitized;
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> sanitizeList(List<Object> list) {
        List<Object> sanitized = new ArrayList<>();
        
        for (Object item : list) {
            if (item instanceof Map) {
                sanitized.add(sanitizeMap((Map<String, Object>) item));
            } else if (item instanceof List) {
                sanitized.add(sanitizeList((List<Object>) item));
            } else {
                sanitized.add(item);
            }
        }
        
        return sanitized;
    }
    
    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_PATTERNS.stream()
            .anyMatch(pattern -> pattern.matcher(fieldName).matches());
    }
    
    private PerformanceMetrics collectPerformanceMetrics(long executionTimeMs) {
        var memoryUsage = memoryBean.getHeapMemoryUsage();
        
        return PerformanceMetrics.builder()
            .executionTimeMs(executionTimeMs)
            .memoryUsedMB(memoryUsage.getUsed() / 1024 / 1024)
            .memoryMaxMB(memoryUsage.getMax() / 1024 / 1024)
            .threadName(Thread.currentThread().getName())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize log entry to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
    
    // Inner classes for structured logging
    
    @lombok.Data
    @lombok.Builder
    public static class LogEntry {
        private LocalDateTime timestamp;
        private String executionId;
        private String serviceCode;
        private String serviceDescription;
        private String stepName;
        private String phase;
        private String message;
        private Long executionTimeMs;
        private Object request;
        private Object response;
        private Object stepData;
        private ErrorInfo error;
        private PerformanceMetrics performanceMetrics;
        private List<String> tags;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ErrorInfo {
        private String type;
        private String message;
        private String stackTrace;
        private String rootCause;
        
        public static ErrorInfo from(Throwable throwable) {
            var builder = ErrorInfo.builder()
                .type(throwable.getClass().getSimpleName())
                .message(throwable.getMessage());
            
            // Include stack trace for debugging (can be controlled by configuration)
            var stackTrace = Arrays.stream(throwable.getStackTrace())
                .limit(10) // Limit stack trace depth
                .map(StackTraceElement::toString)
                .reduce("", (a, b) -> a + "\n" + b);
            builder.stackTrace(stackTrace);
            
            // Find root cause
            Throwable rootCause = throwable;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            if (rootCause != throwable) {
                builder.rootCause(rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage());
            }
            
            return builder.build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PerformanceMetrics {
        private Long executionTimeMs;
        private Long memoryUsedMB;
        private Long memoryMaxMB;
        private String threadName;
        private LocalDateTime timestamp;
    }
}