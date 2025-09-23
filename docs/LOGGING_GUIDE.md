# FlowStep Logging Guide

This guide explains how to use the comprehensive logging capabilities of FlowStep's `@QueryFlow` and `@CommandFlow` annotations.

## Overview

FlowStep provides advanced logging capabilities that can be enabled at the service level through annotation parameters. When enabled, the logging system provides:

- 🚀 **Service execution tracking** with start/end timestamps
- 📊 **Performance metrics** including execution time and memory usage
- 🔍 **Step-by-step execution details** for debugging
- 🛡️ **Automatic data sanitization** to protect sensitive information
- 🏷️ **Structured logging** with contextual information and tags
- 📈 **Audit trails** for command operations
- ⚡ **Configurable log levels** for fine-grained control

## Configuration

### Global Configuration

Enable FlowStep logging in your `application.yml`:

```yaml
flowstep:
  logging:
    enabled: true                           # Enable logging globally
    force-logging-enabled: false            # Force logging even when service-level logging is disabled
    include-stack-traces: false             # Include stack traces in error logs
    max-request-response-size: 10000        # Maximum size of logged request/response data
    sensitive-field-patterns:               # Additional patterns for sensitive data detection
      - ".*api.*key.*"
      - ".*client.*secret.*"
    default-log-level: "INFO"               # Default log level
```

### Service-Level Configuration

Configure logging for individual services using annotation parameters:

#### QueryFlow Services

```java
@Service
@QueryFlow(
    code = "USER_ORDER_SUMMARY",
    desc = "Retrieve user order summary",
    enableLogging = true,                    // Enable logging for this service
    logLevel = QueryFlow.LogLevel.INFO,      // Set log level
    includeRequestResponse = true,           // Include request/response data
    includePerformanceMetrics = true,        // Include performance metrics
    tags = {"user-service", "analytics"}     // Custom tags for filtering
)
public class UserOrderSummaryQueryService extends QueryTemplate<Request, Response> {
    // Service implementation
}
```

#### CommandFlow Services

```java
@Service
@CommandFlow(
    code = "CREATE_ORDER",
    desc = "Create new customer order",
    enableLogging = true,                    // Enable logging for this service
    logLevel = CommandFlow.LogLevel.INFO,    // Set log level
    includeRequestResponse = true,           // Include request/response data
    includePerformanceMetrics = true,        // Include performance metrics
    includeAuditInfo = true,                 // Include audit information
    tags = {"order-service", "business-critical"}
)
public class CreateOrderCommandService extends CommandTemplate<Command, Response> {
    // Service implementation
}
```

## Annotation Parameters

### Common Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `enableLogging` | boolean | false | Enable detailed logging for this service |
| `logLevel` | LogLevel | INFO | Log level (TRACE, DEBUG, INFO, WARN, ERROR) |
| `includeRequestResponse` | boolean | true | Include sanitized request/response data |
| `includePerformanceMetrics` | boolean | true | Include execution time and memory metrics |
| `tags` | String[] | {} | Custom tags for log filtering and searching |

### CommandFlow-Specific Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `includeAuditInfo` | boolean | true | Include audit trail and transaction information |

## Log Output Examples

### Service Execution Start

```json
{
  "timestamp": "2023-12-07T10:30:15.123",
  "executionId": "a1b2c3d4",
  "serviceCode": "USER_ORDER_SUMMARY",
  "serviceDescription": "Retrieve user order summary",
  "phase": "START",
  "message": "Service execution started",
  "request": {
    "userId": 12345,
    "startDate": "2023-01-01",
    "endDate": "2023-12-31",
    "password": "***MASKED***"
  },
  "tags": ["user-service", "analytics"]
}
```

### Step Execution

```json
{
  "timestamp": "2023-12-07T10:30:15.200",
  "executionId": "a1b2c3d4",
  "serviceCode": "USER_ORDER_SUMMARY",
  "stepName": "FetchUserStep",
  "phase": "COMPLETE",
  "message": "Step FetchUserStep: COMPLETE",
  "executionTimeMs": 45,
  "stepData": {
    "userId": 12345,
    "userName": "john.doe"
  }
}
```

### Service Completion with Performance Metrics

```json
{
  "timestamp": "2023-12-07T10:30:16.890",
  "executionId": "a1b2c3d4",
  "serviceCode": "USER_ORDER_SUMMARY",
  "phase": "END",
  "message": "Service execution completed successfully",
  "executionTimeMs": 1767,
  "response": {
    "totalOrders": 42,
    "totalSpent": 1234.56
  },
  "performanceMetrics": {
    "executionTimeMs": 1767,
    "memoryUsedMB": 128,
    "memoryMaxMB": 512,
    "threadName": "http-nio-8080-exec-1"
  }
}
```

### Error Logging

```json
{
  "timestamp": "2023-12-07T10:30:15.500",
  "executionId": "a1b2c3d4",
  "serviceCode": "USER_ORDER_SUMMARY",
  "phase": "ERROR",
  "message": "Service execution failed",
  "executionTimeMs": 377,
  "error": {
    "type": "BusinessException",
    "message": "User not found",
    "stackTrace": "...",
    "rootCause": "EntityNotFoundException: User with ID 12345 not found"
  }
}
```

## Data Sanitization

FlowStep automatically sanitizes sensitive data in logs. The following field patterns are masked by default:

- `.*password.*`
- `.*token.*`
- `.*secret.*`
- `.*key.*`
- `.*auth.*`
- `.*credential.*`
- `.*ssn.*`
- `.*credit.*card.*`
- `.*cvv.*`

Sensitive values are replaced with `***MASKED***`.

### Custom Sensitive Patterns

Add custom patterns in configuration:

```yaml
flowstep:
  logging:
    sensitive-field-patterns:
      - ".*api.*key.*"
      - ".*client.*id.*"
      - ".*internal.*code.*"
```

## Log Levels

### Available Levels

- **TRACE**: Most detailed logging, includes all internal operations
- **DEBUG**: Detailed information for debugging
- **INFO**: General information about service execution (default)
- **WARN**: Warning messages for potential issues
- **ERROR**: Error conditions and exceptions

### Setting Log Levels

#### Global Default

```yaml
flowstep:
  logging:
    default-log-level: "DEBUG"
```

#### Service-Specific

```java
@QueryFlow(
    code = "SENSITIVE_QUERY",
    logLevel = QueryFlow.LogLevel.WARN  // Only log warnings and errors
)
```

#### Logger-Specific (application.yml)

```yaml
logging:
  level:
    net.xrftech.flowstep.service.USER_ORDER_SUMMARY: DEBUG
    net.xrftech.flowstep.service.CREATE_ORDER: INFO
```

## Best Practices

### 1. Service Classification

Use different log levels based on service criticality:

```java
// Business-critical services
@QueryFlow(enableLogging = true, logLevel = LogLevel.INFO)

// Development/debugging services  
@QueryFlow(enableLogging = true, logLevel = LogLevel.DEBUG)

// High-frequency services (reduce noise)
@QueryFlow(enableLogging = true, logLevel = LogLevel.WARN)
```

### 2. Meaningful Tags

Use descriptive tags for better log filtering:

```java
@QueryFlow(
    tags = {
        "user-management",      // Domain
        "high-frequency",       // Performance characteristic
        "external-api",         // Integration type
        "business-critical"     // Business importance
    }
)
```

### 3. Performance Considerations

- Enable logging selectively for critical services
- Use higher log levels (WARN/ERROR) for high-frequency services
- Monitor log volume and adjust `max-request-response-size`

### 4. Security

- Review and extend sensitive field patterns
- Avoid logging sensitive business data even when masked
- Use audit logs for compliance requirements

## Troubleshooting

### Logging Not Working

1. Check global configuration:
   ```yaml
   flowstep:
     logging:
       enabled: true
   ```

2. Verify service annotation:
   ```java
   @QueryFlow(enableLogging = true)
   ```

3. Check logger configuration:
   ```yaml
   logging:
     level:
       net.xrftech.flowstep.service: INFO
   ```

### Performance Issues

1. Reduce log level for high-frequency services
2. Decrease `max-request-response-size`
3. Disable performance metrics for non-critical services:
   ```java
   @QueryFlow(includePerformanceMetrics = false)
   ```

### Missing Context Information

1. Ensure MDC is properly configured
2. Use custom log pattern with execution ID:
   ```yaml
   logging:
     pattern:
       console: "%d [%X{executionId:-}] [%X{serviceCode:-}] %logger - %msg%n"
   ```

## Integration Examples

### ELK Stack

Configure structured JSON logging for Elasticsearch:

```yaml
logging:
  pattern:
    file: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss.SSS}","level":"%level","thread":"%thread","logger":"%logger","executionId":"%X{executionId:-}","serviceCode":"%X{serviceCode:-}","message":"%message"}%n'
```

### Prometheus Metrics

Enable metrics collection:

```yaml
flowstep:
  performance:
    enable-metrics: true
```

### Custom Log Aggregation

Use tags for filtering in log aggregation tools:

```java
@QueryFlow(tags = {"environment:prod", "team:backend", "criticality:high"})
```

## Migration Guide

### Enabling Logging for Existing Services

1. Add logging parameters to existing annotations:
   ```java
   // Before
   @QueryFlow(code = "USER_QUERY", desc = "Get user")
   
   // After  
   @QueryFlow(
       code = "USER_QUERY", 
       desc = "Get user",
       enableLogging = true,
       logLevel = LogLevel.INFO
   )
   ```

2. Update configuration:
   ```yaml
   flowstep:
     logging:
       enabled: true
   ```

3. Test and adjust log levels based on volume

### Performance Testing

Monitor performance impact when enabling logging:

1. Measure baseline performance without logging
2. Enable logging with INFO level
3. Compare performance metrics
4. Adjust log levels and parameters as needed

## Conclusion

FlowStep's logging capabilities provide comprehensive observability for your services while maintaining security through automatic data sanitization. Use the configuration options to balance between detailed monitoring and performance requirements.

For additional support or questions, refer to the main FlowStep documentation or create an issue in the project repository.