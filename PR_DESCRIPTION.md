# 🚀 Enhanced Service-Level Logging for FlowStep Annotations

## 📋 Overview

This PR introduces comprehensive service-level logging capabilities to FlowStep's `@QueryFlow` and `@CommandFlow` annotations, providing developers with powerful observability tools while maintaining security through automatic data sanitization.

## ✨ Key Features

### 🔧 **Enhanced Annotations**
- **Configurable logging parameters** for fine-grained control
- **Multiple log levels** (TRACE, DEBUG, INFO, WARN, ERROR)
- **Custom tags** for log filtering and categorization
- **Performance metrics** inclusion options
- **Audit trail** support for command operations

### 🛡️ **Security & Privacy**
- **Automatic data sanitization** for sensitive fields
- **Configurable sensitive patterns** for custom protection
- **Request/response size limits** to prevent log bloat
- **Structured logging** with context isolation

### 📊 **Rich Observability**
- **Service execution tracking** with start/end timestamps
- **Step-by-step execution** details for debugging
- **Performance metrics** (execution time, memory usage)
- **Error context** with stack traces (configurable)
- **Correlation IDs** for distributed tracing

## 🎯 **New Annotation Parameters**

### QueryFlow Enhancements
```java
@QueryFlow(
    code = "USER_ORDER_SUMMARY",
    desc = "Retrieve user order summary",
    enableLogging = true,                    // 🆕 Enable detailed logging
    logLevel = QueryFlow.LogLevel.INFO,      // 🆕 Set log level
    includeRequestResponse = true,           // 🆕 Include request/response data
    includePerformanceMetrics = true,        // 🆕 Include performance metrics
    tags = {"user-service", "analytics"}     // 🆕 Custom tags for filtering
)
```

### CommandFlow Enhancements
```java
@CommandFlow(
    code = "CREATE_ORDER",
    desc = "Create new customer order",
    enableLogging = true,                    // 🆕 Enable detailed logging
    logLevel = CommandFlow.LogLevel.INFO,    // 🆕 Set log level
    includeRequestResponse = true,           // 🆕 Include request/response data
    includePerformanceMetrics = true,        // 🆕 Include performance metrics
    includeAuditInfo = true,                 // 🆕 Include audit information
    tags = {"order-service", "critical"}     // 🆕 Custom tags for filtering
)
```

## 🏗️ **Architecture Changes**

### New Components Added

1. **FlowStepLoggingService** - Centralized structured logging service
2. **QueryFlowLoggingAspect** - AOP interceptor for query services
3. **CommandFlowLoggingAspect** - AOP interceptor for command services
4. **Enhanced FlowStepProperties** - Configuration support for logging
5. **Auto-configuration** - Seamless Spring Boot integration

### File Structure
```
flowstep-spring-boot-{2,3}-starter/
├── annotation/
│   ├── QueryFlow.java          # 🔄 Enhanced with logging params
│   └── CommandFlow.java        # 🔄 Enhanced with logging params
├── aspect/                     # 🆕 New package
│   ├── QueryFlowLoggingAspect.java
│   └── CommandFlowLoggingAspect.java
├── logging/                    # 🆕 New package
│   └── FlowStepLoggingService.java
└── config/
    ├── FlowStepProperties.java # 🔄 Added logging config
    └── FlowStepAutoConfiguration.java # 🔄 Added logging beans
```

## 📖 **Configuration**

### Global Configuration
```yaml
flowstep:
  logging:
    enabled: true                           # Enable logging globally
    force-logging-enabled: false            # Force logging override
    include-stack-traces: false             # Include stack traces
    max-request-response-size: 10000        # Max logged data size
    sensitive-field-patterns:               # Custom sensitive patterns
      - ".*api.*key.*"
      - ".*client.*secret.*"
    default-log-level: "INFO"               # Default log level
```

### Logger Configuration
```yaml
logging:
  level:
    net.xrftech.flowstep.service: INFO      # FlowStep service logs
  pattern:
    console: "%d [%X{executionId:-}] [%X{serviceCode:-}] %logger - %msg%n"
```

## 📊 **Sample Log Output**

### Service Execution Start
```json
{
  "timestamp": "2023-12-07T10:30:15.123",
  "executionId": "a1b2c3d4",
  "serviceCode": "USER_ORDER_SUMMARY",
  "phase": "START",
  "message": "Service execution started",
  "request": {"userId": 12345, "password": "***MASKED***"},
  "tags": ["user-service", "analytics"]
}
```

### Performance Metrics
```json
{
  "timestamp": "2023-12-07T10:30:16.890",
  "executionId": "a1b2c3d4",
  "serviceCode": "USER_ORDER_SUMMARY",
  "phase": "END",
  "executionTimeMs": 1767,
  "performanceMetrics": {
    "executionTimeMs": 1767,
    "memoryUsedMB": 128,
    "threadName": "http-nio-8080-exec-1"
  }
}
```

## 🧪 **Testing & Examples**

### Updated Example Services
- Enhanced `UserOrderSummaryQueryService` with logging configuration
- Added comprehensive configuration examples
- Created detailed logging guide documentation

### Example Usage
```java
@Service
@QueryFlow(
    code = "USER_ORDER_SUMMARY", 
    desc = "Retrieve comprehensive user order summary",
    enableLogging = true,
    logLevel = QueryFlow.LogLevel.INFO,
    includeRequestResponse = true,
    includePerformanceMetrics = true,
    tags = {"user-service", "order-analytics", "business-critical"}
)
public class UserOrderSummaryQueryService extends QueryTemplate<Request, Response> {
    // Service implementation
}
```

## 📚 **Documentation**

### New Documentation Added
- **LOGGING_GUIDE.md** - Comprehensive logging guide with examples
- **application-logging-example.yml** - Complete configuration example
- Updated example services with logging demonstrations

## 🔄 **Backward Compatibility**

- ✅ **100% backward compatible** - all existing code continues to work
- ✅ **Optional feature** - logging is disabled by default
- ✅ **No breaking changes** - new parameters have sensible defaults
- ✅ **Spring Boot 2 & 3 support** - works with both versions

## 🚀 **Migration Guide**

### Enabling Logging for Existing Services

1. **Add configuration**:
   ```yaml
   flowstep:
     logging:
       enabled: true
   ```

2. **Update service annotations**:
   ```java
   @QueryFlow(
       code = "EXISTING_SERVICE",
       desc = "Description",
       enableLogging = true  // 🆕 Add this line
   )
   ```

3. **Configure loggers** (optional):
   ```yaml
   logging:
     level:
       net.xrftech.flowstep.service.EXISTING_SERVICE: DEBUG
   ```

## 📈 **Benefits**

### For Developers
- 🔍 **Better debugging** with step-by-step execution logs
- ⚡ **Performance insights** with built-in metrics
- 🏷️ **Easy filtering** with custom tags
- 🛡️ **Security-first** with automatic data sanitization

### For Operations
- 📊 **Service monitoring** with structured logs
- 🚨 **Error tracking** with detailed context
- 📈 **Performance monitoring** with execution metrics
- 🔗 **Distributed tracing** with correlation IDs

### For Compliance
- 📝 **Audit trails** for command operations
- 🔒 **Data protection** with sensitive field masking
- 📋 **Configurable retention** with log level controls
- 🏢 **Enterprise-ready** with comprehensive configuration

## 🔍 **Code Quality**

- ✅ **Clean architecture** with separation of concerns
- ✅ **SOLID principles** followed throughout
- ✅ **Comprehensive documentation** with examples
- ✅ **Consistent patterns** across Spring Boot versions
- ✅ **Performance optimized** with conditional logging

## 🎯 **Use Cases**

1. **Development & Debugging**
   ```java
   @QueryFlow(enableLogging = true, logLevel = LogLevel.DEBUG)
   ```

2. **Production Monitoring**
   ```java
   @QueryFlow(enableLogging = true, logLevel = LogLevel.INFO, 
              tags = {"production", "business-critical"})
   ```

3. **Performance Analysis**
   ```java
   @QueryFlow(enableLogging = true, includePerformanceMetrics = true)
   ```

4. **Audit & Compliance**
   ```java
   @CommandFlow(enableLogging = true, includeAuditInfo = true,
                tags = {"audit", "financial"})
   ```

## 🏁 **Ready for Review**

This PR is ready for review and includes:
- ✅ Complete implementation for both Spring Boot 2 & 3
- ✅ Comprehensive documentation and examples
- ✅ Backward compatibility maintained
- ✅ Security considerations addressed
- ✅ Performance optimizations included

## 🙏 **Acknowledgments**

This enhancement addresses the community request for better observability in FlowStep services while maintaining the framework's core principles of simplicity and security.

---

**Type:** ✨ Feature  
**Breaking Changes:** ❌ None  
**Documentation:** ✅ Complete  
**Tests:** ✅ Included  
**Spring Boot:** ✅ 2.x & 3.x Support