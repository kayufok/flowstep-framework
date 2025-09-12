# 🔄 FlowStep Spring Boot Starter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep-spring-boot-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/kayufok/flowstep-spring-boot-starter/workflows/CI/badge.svg)](https://github.com/kayufok/flowstep-spring-boot-starter/actions)

> **Clean CQRS Spring Boot Starter for applications of any size - from prototype to production.**

## 🎯 Why FlowStep?

Modern Spring Boot applications need **clean architecture**, **testable code**, and **maintainable business logic**. FlowStep provides:

- 🔥 **CQRS Pattern**: Clean separation between read (Query) and write (Command) operations
- 🏗️ **Template Method**: Enforced execution flow prevents architectural drift
- 🔧 **Step-Based Design**: Highly testable, modular components
- 🛡️ **Type-Safe Error Handling**: Comprehensive error classification and management
- ⚡ **Spring Boot Integration**: Seamless auto-configuration and dependency injection
- 🎯 **Multi-Version Support**: Java 8+ & Spring Boot 2.7.x + Java 17+ & Spring Boot 3.x

## 📦 Choose Your Version

FlowStep provides two starter modules to support different environments:

| Starter | Java Version | Spring Boot Version | Use Case |
|---------|-------------|-------------------|----------|
| **flowstep-spring-boot-2-starter** | Java 8+ | Spring Boot 2.7.x+ | Legacy applications, Java 8 environments |
| **flowstep-spring-boot-3-starter** | Java 17+ | Spring Boot 3.x+ | Modern applications, latest features |

## 🚀 Quick Start

### For Spring Boot 2.7.x + Java 8+

#### Maven
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'net.xrftech:flowstep-spring-boot-2-starter:1.0.0'
```

### For Spring Boot 3.x + Java 17+

#### Maven
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'net.xrftech:flowstep-spring-boot-3-starter:1.0.0'
```

## 🏁 5-Minute Tutorial

### Step 1: Add the Starter
Add the appropriate starter dependency (see above) to your project.

### Step 2: Create Your First Query
```java
@QueryFlow(code = "USER_PROFILE", desc = "Retrieve user profile information")
@Service
public class UserProfileQueryService extends QueryTemplate<UserProfileRequest, UserProfileResponse> {

    private final UserRepository userRepository;

    @Override
    protected List<QueryStep<?>> steps(UserProfileRequest request, QueryContext context) {
        return List.of(
            () -> {
                User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                context.put("user", user);
                return StepResult.success(user);
            }
        );
    }

    @Override
    protected UserProfileResponse buildResponse(QueryContext context) {
        User user = context.get("user");
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail());
    }
}
```

### Step 3: Create Your First Command
```java
@CommandFlow(code = "CREATE_ORDER", desc = "Process new customer order")
@Service
@Transactional
public class CreateOrderService extends CommandTemplate<CreateOrderCommand, OrderCreatedResponse> {

    private final OrderRepository orderRepository;

    @Override
    protected List<CommandStep<?>> steps(CreateOrderCommand command, CommandContext context) {
        return List.of(
            () -> {
                Order order = new Order(command.getUserId(), command.getItems());
                Order savedOrder = orderRepository.save(order);
                context.put("order", savedOrder);
                return StepResult.success(savedOrder);
            }
        );
    }

    @Override
    protected OrderCreatedResponse buildResponse(CommandContext context) {
        Order order = context.get("order");
        return new OrderCreatedResponse(order.getId(), order.getOrderNumber());
    }
}
```

### Step 4: Use in Your Controller
```java
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserProfileQueryService userProfileQuery;
    private final CreateOrderService createOrderCommand;

    @GetMapping("/users/{id}")
    public UserProfileResponse getUser(@PathVariable Long id) throws BusinessException {
        return userProfileQuery.execute(new UserProfileRequest(id));
    }

    @PostMapping("/orders")
    public OrderCreatedResponse createOrder(@RequestBody CreateOrderCommand command) throws BusinessException {
        return createOrderCommand.execute(command);
    }
}
```

That's it! FlowStep automatically configures itself and provides global exception handling.

## ⚙️ Configuration

FlowStep can be configured through `application.properties` or `application.yml`:

```properties
# Enable/disable FlowStep (default: true)
flowstep.enabled=true

# Enable/disable global exception handler (default: true)
flowstep.exception-handler.enabled=true

# Include stack traces in error responses (default: false)
flowstep.exception-handler.include-stack-trace=false
```

Or with YAML:
```yaml
flowstep:
  enabled: true
  exception-handler:
    enabled: true
    include-stack-trace: false
```

## 📚 Core Concepts

### **Template Pattern Enforcement**
Every service follows a consistent 4-step flow:
1. **Validation** - Request/command validation
2. **Context Setup** - Shared data initialization  
3. **Step Execution** - Business logic in composable steps
4. **Response Building** - Result aggregation

### **CQRS Separation**
- **Query Services**: Read-only, no transactions, cacheable
- **Command Services**: Write operations, transactional, auditable

### **Error Handling Strategy**
```java
public enum ErrorType {
    VALIDATION,  // Client-side fixable (400 Bad Request)
    BUSINESS,    // Business rule violations (409 Conflict)  
    SYSTEM       // Technical failures (500 Internal Server Error)
}
```

### **Context-Based Communication**
Steps communicate through type-safe context objects:
```java
// Store data for other steps
context.put("userId", user.getId());
context.put("calculatedTotal", total);

// Retrieve data from previous steps
User user = context.get("user");
BigDecimal total = context.getOrDefault("total", BigDecimal.ZERO);
```

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐
│   Controller    │    │   Controller    │
│    (HTTP)       │    │    (HTTP)       │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│  QueryTemplate  │    │ CommandTemplate │
│   (Read-Only)   │    │ (Transactional) │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│   QueryStep     │    │  CommandStep    │
│   QueryStep     │    │  CommandStep    │
│   QueryStep     │    │  CommandStep    │
└─────────────────┘    └─────────────────┘
```

## 🧪 Testing

### **Unit Testing Steps**
```java
class FetchUserStepTest {
    
    @Test
    void shouldFetchUserSuccessfully() {
        // Given
        QueryContext context = new QueryContext();
        context.setRequest(new UserProfileRequest(123L));
        
        // When
        StepResult<User> result = fetchUserStep.execute(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.<User>get("user")).isNotNull();
    }
}
```

### **Integration Testing Services**
```java
@SpringBootTest
class UserProfileQueryServiceTest {
    
    @Test
    void shouldRetrieveCompleteUserProfile() throws Exception {
        // Given
        UserProfileRequest request = new UserProfileRequest(userId, true);
        
        // When
        UserProfileResponse response = userProfileService.execute(request);
        
        // Then
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getPreferences()).isNotNull();
    }
}
```

### **Optional Architecture Testing**
FlowStep includes optional ArchUnit support for architecture validation:

```bash
# Enable architecture tests
./gradlew test -PenableArchUnit=true
```

Add ArchUnit dependency if you want architecture testing:
```gradle
testImplementation 'com.tngtech.archunit:archunit-junit5:1.2.1'
```

## 🔄 Migration Guide

### From Plain Spring Services
1. Add the appropriate FlowStep starter dependency
2. Extend `QueryTemplate` for read operations
3. Extend `CommandTemplate` for write operations  
4. Break down logic into composable steps
5. Use context for step communication

### Version Migration
- **From 2.x to 3.x**: Change dependency from `flowstep-spring-boot-2-starter` to `flowstep-spring-boot-3-starter`
- **Java Version**: Ensure Java 17+ when using Spring Boot 3.x starter

## 📚 Documentation

For detailed documentation, please see:

- **[Complete Documentation Index](docs/README.md)** - Central hub for all documentation
- **[API Reference](docs/API_REFERENCE.md)** - Complete API documentation
- **[Architecture Guide](docs/ARCHITECTURE.md)** - Design patterns and system architecture
- **[Usage Guide](docs/USAGE_GUIDE.md)** - Practical examples and patterns
- **[Testing Guide](docs/TESTING_GUIDE.md)** - Testing strategies and best practices
- **[Configuration Reference](docs/CONFIGURATION_REFERENCE.md)** - All configuration options

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### **Development Setup**
```bash
# Clone repository
git clone https://github.com/kayufok/flowstep-spring-boot-starter.git
cd flowstep-spring-boot-starter

# Build all modules
./gradlew build

# Test specific module
./gradlew :flowstep-spring-boot-2-starter:test
./gradlew :flowstep-spring-boot-3-starter:test

# Run with architecture tests
./gradlew test -PenableArchUnit=true
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- Inspired by enterprise patterns from Eric Evans' Domain-Driven Design
- CQRS concepts from Greg Young and Udi Dahan  
- Template Method pattern from Gang of Four Design Patterns
- Spring Boot team for excellent framework foundation

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/kayufok/flowstep-spring-boot-starter/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kayufok/flowstep-spring-boot-starter/discussions)  

## 🔗 Related Projects

- **Examples**: [FlowStep Examples Repository](https://github.com/kayufok/flowstep-examples)
- **Documentation**: [FlowStep Documentation](https://flowstep.xrftech.net)

---

**⭐ If this starter helps your Spring Boot development, please consider giving it a star!**

*Made with ❤️ for Spring Boot developers*