# ⚙️ FlowStep Configuration Reference

## Table of Contents
1. [Configuration Overview](#configuration-overview)
2. [Core Properties](#core-properties)
3. [Spring Boot Version Differences](#spring-boot-version-differences)
4. [Auto-Configuration](#auto-configuration)
5. [Conditional Beans](#conditional-beans)
6. [Custom Configuration](#custom-configuration)
7. [Environment-Specific Configuration](#environment-specific-configuration)
8. [Advanced Configuration](#advanced-configuration)

## Configuration Overview

FlowStep uses Spring Boot's configuration mechanism, supporting:
- `application.properties`
- `application.yml` / `application.yaml`
- Environment variables
- Command-line arguments
- Configuration classes

### Configuration Hierarchy

```
Command Line Args
       ↓
Environment Variables
       ↓
application-{profile}.yml
       ↓
application.yml
       ↓
Default Values
```

## Core Properties

### Basic Configuration

#### Properties Format
```properties
# Enable/disable FlowStep framework
flowstep.enabled=true

# Enable/disable global exception handler
flowstep.exception-handler.enabled=true

# Include stack traces in error responses
flowstep.exception-handler.include-stack-trace=false
```

#### YAML Format
```yaml
flowstep:
  enabled: true
  exception-handler:
    enabled: true
    include-stack-trace: false
```

### Property Details

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `flowstep.enabled` | boolean | `true` | Master switch to enable/disable FlowStep |
| `flowstep.exception-handler.enabled` | boolean | `true` | Enable global exception handling |
| `flowstep.exception-handler.include-stack-trace` | boolean | `false` | Include stack traces in error responses |

### Environment Variable Format

```bash
# Linux/Mac
export FLOWSTEP_ENABLED=true
export FLOWSTEP_EXCEPTION_HANDLER_ENABLED=true
export FLOWSTEP_EXCEPTION_HANDLER_INCLUDE_STACK_TRACE=false

# Windows
set FLOWSTEP_ENABLED=true
set FLOWSTEP_EXCEPTION_HANDLER_ENABLED=true
set FLOWSTEP_EXCEPTION_HANDLER_INCLUDE_STACK_TRACE=false
```

## Spring Boot Version Differences

### Spring Boot 2.x Configuration

**Location:** `META-INF/spring.factories`

```properties
# Auto Configuration
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  net.xrftech.flowstep.config.FlowStepAutoConfiguration
```

**Dependencies:**
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Java Version:** 8+

### Spring Boot 3.x Configuration

**Location:** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
net.xrftech.flowstep.config.FlowStepAutoConfiguration
```

**Dependencies:**
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Java Version:** 17+

## Auto-Configuration

### FlowStepAutoConfiguration Class

```java
@AutoConfiguration  // Spring Boot 3.x
// @Configuration   // Spring Boot 2.x
@ConditionalOnProperty(
    name = "flowstep.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(FlowStepProperties.class)
public class FlowStepAutoConfiguration {
    
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    @ConditionalOnProperty(
        name = "flowstep.exception-handler.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public GlobalExceptionHandler flowStepGlobalExceptionHandler(
            FlowStepProperties properties) {
        return new GlobalExceptionHandler(properties);
    }
}
```

### Configuration Properties Class

```java
@ConfigurationProperties(prefix = "flowstep")
public class FlowStepProperties {
    
    private boolean enabled = true;
    private ExceptionHandler exceptionHandler = new ExceptionHandler();
    
    // Getters and setters
    
    public static class ExceptionHandler {
        private boolean enabled = true;
        private boolean includeStackTrace = false;
        
        // Getters and setters
    }
}
```

## Conditional Beans

### Understanding Conditionals

FlowStep uses Spring Boot conditionals to control bean creation:

#### @ConditionalOnProperty
```java
@ConditionalOnProperty(
    name = "flowstep.enabled",
    havingValue = "true",
    matchIfMissing = true  // Default to true if property missing
)
```

#### @ConditionalOnClass
```java
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
// Only create bean if Spring Web is on classpath
```

#### @ConditionalOnMissingBean
```java
@ConditionalOnMissingBean
// Only create if no other bean of this type exists
```

### Disabling Components

```yaml
# Disable entire FlowStep framework
flowstep:
  enabled: false

# Disable only exception handler
flowstep:
  exception-handler:
    enabled: false
```

## Custom Configuration

### Extending Configuration Properties

```java
@Component
@ConfigurationProperties(prefix = "myapp.flowstep")
public class CustomFlowStepProperties {
    
    private int maxRetries = 3;
    private Duration timeout = Duration.ofSeconds(30);
    private boolean auditEnabled = true;
    private Map<String, String> customHeaders = new HashMap<>();
    
    // Getters and setters
}
```

```yaml
myapp:
  flowstep:
    max-retries: 5
    timeout: PT45S  # ISO-8601 duration
    audit-enabled: true
    custom-headers:
      X-Request-ID: ${random.uuid}
      X-Service-Name: my-service
```

### Custom Auto-Configuration

```java
@Configuration
@AutoConfigureAfter(FlowStepAutoConfiguration.class)
@ConditionalOnProperty(
    prefix = "myapp.flowstep",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class CustomFlowStepAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService(CustomFlowStepProperties properties) {
        return new AuditService(properties.isAuditEnabled());
    }
    
    @Bean
    public RetryTemplate retryTemplate(CustomFlowStepProperties properties) {
        RetryTemplate template = new RetryTemplate();
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getMaxRetries());
        template.setRetryPolicy(retryPolicy);
        
        return template;
    }
}
```

### Overriding Default Beans

```java
@Configuration
public class CustomExceptionHandlerConfig {
    
    @Bean
    @Primary  // Override FlowStep's default
    public GlobalExceptionHandler customGlobalExceptionHandler() {
        return new CustomGlobalExceptionHandler();
    }
}

public class CustomGlobalExceptionHandler extends GlobalExceptionHandler {
    
    @Override
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        // Custom handling logic
        ErrorResponse response = ErrorResponse.builder()
            .errorCode(ex.getErrorCode())
            .message(translateMessage(ex.getMessage()))  // Custom translation
            .timestamp(LocalDateTime.now())
            .traceId(MDC.get("traceId"))  // Add trace ID
            .build();
        
        return ResponseEntity.status(determineStatus(ex)).body(response);
    }
}
```

## Environment-Specific Configuration

### Development Configuration

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev

flowstep:
  enabled: true
  exception-handler:
    enabled: true
    include-stack-trace: true  # Show stack traces in dev

logging:
  level:
    net.xrftech.flowstep: DEBUG
    
# Custom dev properties
myapp:
  flowstep:
    audit-enabled: false  # Disable audit in dev
    timeout: PT60S  # Longer timeout for debugging
```

### Production Configuration

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod

flowstep:
  enabled: true
  exception-handler:
    enabled: true
    include-stack-trace: false  # Hide stack traces in prod

logging:
  level:
    net.xrftech.flowstep: WARN
    
# Custom prod properties
myapp:
  flowstep:
    audit-enabled: true
    timeout: PT10S  # Shorter timeout in prod
    max-retries: 3
```

### Testing Configuration

```yaml
# application-test.yml
spring:
  profiles:
    active: test

flowstep:
  enabled: true
  exception-handler:
    enabled: false  # Disable for testing

# Test-specific properties
myapp:
  flowstep:
    audit-enabled: false
    max-retries: 1  # No retries in tests
```

### Profile Activation

```bash
# Via command line
java -jar app.jar --spring.profiles.active=prod

# Via environment variable
export SPRING_PROFILES_ACTIVE=prod

# Via application.yml
spring:
  profiles:
    active: prod
```

## Advanced Configuration

### Logging Configuration

```yaml
logging:
  level:
    # FlowStep framework logging
    net.xrftech.flowstep: INFO
    net.xrftech.flowstep.QueryTemplate: DEBUG
    net.xrftech.flowstep.CommandTemplate: DEBUG
    net.xrftech.flowstep.exception: WARN
    
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger - %msg%n"
    
  file:
    name: logs/flowstep.log
    max-size: 10MB
    max-history: 30
```

### Metrics Configuration

```java
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class FlowStepMetricsConfiguration {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public MeterBinder flowStepMetrics() {
        return (registry) -> {
            Gauge.builder("flowstep.enabled", this, 
                config -> flowStepProperties.isEnabled() ? 1 : 0)
                .description("FlowStep framework status")
                .register(registry);
        };
    }
}
```

```yaml
management:
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Transaction Configuration

```java
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {
    
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(dataSource);
        manager.setDefaultTimeout(30);  // 30 seconds default
        return manager;
    }
    
    @Bean
    public TransactionTemplate transactionTemplate(
            PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate();
        template.setTransactionManager(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return template;
    }
}
```

### Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    @Bean
    public TaskExecutor flowStepTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("flowstep-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async execution failed in method: {}", 
                method.getName(), throwable);
        };
    }
}
```

### Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats());
        cacheManager.setCacheNames(List.of("queries", "users", "products"));
        return cacheManager;
    }
    
    @Bean
    @ConditionalOnProperty(
        name = "flowstep.cache.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public CacheInterceptor flowStepCacheInterceptor() {
        return new FlowStepCacheInterceptor();
    }
}
```

```yaml
flowstep:
  cache:
    enabled: true
    
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m
```

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
            );
        
        return http.build();
    }
}
```

### Event Configuration

```java
@Configuration
public class EventConfiguration {
    
    @Bean
    @ConditionalOnProperty(
        name = "flowstep.events.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = 
            new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(flowStepTaskExecutor());
        return eventMulticaster;
    }
    
    @Bean
    public EventPublisher eventPublisher(ApplicationEventPublisher publisher) {
        return new SpringEventPublisher(publisher);
    }
}
```

## Configuration Best Practices

### 1. Use Profiles Effectively

```yaml
# Base configuration (application.yml)
flowstep:
  enabled: true

---
# Development profile
spring:
  config:
    activate:
      on-profile: dev
      
flowstep:
  exception-handler:
    include-stack-trace: true

---
# Production profile
spring:
  config:
    activate:
      on-profile: prod
      
flowstep:
  exception-handler:
    include-stack-trace: false
```

### 2. Externalize Sensitive Configuration

```yaml
# application.yml
database:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
  username: ${DB_USERNAME:postgres}
  password: ${DB_PASSWORD:changeme}
  
api:
  key: ${API_KEY:}
  secret: ${API_SECRET:}
```

### 3. Document Custom Properties

```java
/**
 * Custom FlowStep configuration properties.
 * 
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "myapp.flowstep")
@Validated
public class CustomProperties {
    
    /**
     * Maximum number of retry attempts for failed operations.
     * Default: 3
     */
    @Min(1)
    @Max(10)
    private int maxRetries = 3;
    
    /**
     * Timeout duration for external service calls.
     * Default: 30 seconds
     */
    @NotNull
    private Duration timeout = Duration.ofSeconds(30);
}
```

### 4. Validate Configuration

```java
@Component
@ConditionalOnProperty(name = "flowstep.validation.enabled", havingValue = "true")
public class ConfigurationValidator implements InitializingBean {
    
    @Autowired
    private FlowStepProperties properties;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        validateConfiguration();
    }
    
    private void validateConfiguration() {
        if (properties.isEnabled() && 
            properties.getExceptionHandler().isEnabled() &&
            !isSpringWebPresent()) {
            throw new IllegalStateException(
                "Exception handler requires Spring Web on classpath"
            );
        }
    }
    
    private boolean isSpringWebPresent() {
        try {
            Class.forName("org.springframework.web.servlet.DispatcherServlet");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
```

### 5. Configuration Metadata

Create `META-INF/spring-configuration-metadata.json`:

```json
{
  "properties": [
    {
      "name": "flowstep.enabled",
      "type": "java.lang.Boolean",
      "description": "Enable or disable FlowStep framework.",
      "defaultValue": true
    },
    {
      "name": "flowstep.exception-handler.enabled",
      "type": "java.lang.Boolean",
      "description": "Enable or disable global exception handler.",
      "defaultValue": true
    },
    {
      "name": "flowstep.exception-handler.include-stack-trace",
      "type": "java.lang.Boolean",
      "description": "Include stack traces in error responses.",
      "defaultValue": false
    }
  ],
  "hints": [
    {
      "name": "flowstep.enabled",
      "values": [
        {
          "value": true,
          "description": "Enable FlowStep framework."
        },
        {
          "value": false,
          "description": "Disable FlowStep framework."
        }
      ]
    }
  ]
}
```

## Troubleshooting Configuration

### Common Issues

#### 1. Auto-configuration not working

**Check:**
- Starter dependency is included
- `@SpringBootApplication` or `@EnableAutoConfiguration` present
- No `@ComponentScan` excluding FlowStep packages
- Check auto-configuration report: `--debug` flag

#### 2. Properties not being loaded

**Check:**
- Property prefix matches exactly
- `@EnableConfigurationProperties` or `@ConfigurationPropertiesScan` present
- Property file in correct location (`src/main/resources`)
- Property syntax is correct (YAML indentation)

#### 3. Conditional beans not created

**Check:**
- Required classes on classpath
- Property conditions met
- No conflicting beans already registered
- Check conditions report: `--debug` flag

### Debug Configuration

```bash
# Enable auto-configuration report
java -jar app.jar --debug

# Enable specific logging
java -jar app.jar --logging.level.org.springframework.boot.autoconfigure=DEBUG

# Print all configuration properties
java -jar app.jar --spring.config.debug=true
```

### Configuration Actuator Endpoint

```yaml
management:
  endpoints:
    web:
      exposure:
        include: configprops,env,beans
```

Access endpoints:
- `/actuator/configprops` - All configuration properties
- `/actuator/env` - Environment properties
- `/actuator/beans` - All Spring beans

## Conclusion

FlowStep's configuration system leverages Spring Boot's powerful configuration capabilities while providing sensible defaults. The framework can be easily customized through properties, extended with custom configuration classes, and integrated with existing Spring Boot applications seamlessly.