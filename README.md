# 🔄 FlowStep Framework

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/kayufok/flowstep-framework/workflows/CI/badge.svg)](https://github.com/kayufok/flowstep-framework/actions)

> **Clean CQRS framework for Spring Boot applications of any size - from prototype to production.**

## 🎯 Why FlowStep?

Modern applications need **clean architecture**, **testable code**, and **maintainable business logic**. FlowStep provides:

- 🔥 **CQRS Pattern**: Clean separation between read (Query) and write (Command) operations
- 🏗️ **Template Method**: Enforced execution flow prevents architectural drift
- 🔧 **Step-Based Design**: Highly testable, modular components
- 🛡️ **Type-Safe Error Handling**: Comprehensive error classification and management
- ⚡ **Spring Boot Integration**: Seamless auto-configuration and dependency injection

## 🚀 Quick Start

### Maven
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle
```gradle
implementation 'net.xrftech:flowstep:1.0.0-SNAPSHOT'
```

### Basic Usage

**1. Create a Query Service (Read Operation):**

```java
@QueryFlow(code = "USER_PROFILE", desc = "Retrieve user profile information")
@Service
public class UserProfileQueryService extends QueryTemplate<UserProfileRequest, UserProfileResponse> {

    private final UserRepository userRepository;
    private final PreferencesService preferencesService;

    @Override
    protected List<QueryStep<?>> steps(UserProfileRequest request, QueryContext context) {
        List<QueryStep<?>> steps = new ArrayList<>();
        
        // Always fetch user data
        steps.add(new FetchUserStep(userRepository));
        
        // Conditionally fetch preferences
        if (request.isIncludePreferences()) {
            steps.add(new FetchPreferencesStep(preferencesService));
        }
        
        return steps;
    }

    @Override
    protected UserProfileResponse buildResponse(QueryContext context) {
        User user = context.get("user");
        UserPreferences preferences = context.get("preferences");
        return new UserProfileResponse(user, preferences);
    }
}
```

**2. Create a Command Service (Write Operation):**

```java
@CommandFlow(code = "CREATE_ORDER", desc = "Process new customer order")
@Service
@Transactional
public class CreateOrderService extends CommandTemplate<CreateOrderCommand, OrderCreatedResponse> {

    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @Override
    protected List<CommandStep<?>> steps(CreateOrderCommand command, CommandContext context) {
        return List.of(
            new ValidateInventoryStep(inventoryService),
            new ProcessPaymentStep(paymentService),
            new CreateOrderStep(orderRepository),
            new SendConfirmationStep()
        );
    }

    @Override
    protected OrderCreatedResponse buildResponse(CommandContext context) {
        Order createdOrder = context.get("order");
        return new OrderCreatedResponse(createdOrder.getId(), createdOrder.getOrderNumber());
    }
}
```

**3. Implement Steps:**

```java
@Component
public class FetchUserStep implements QueryStep<User> {
    
    private final UserRepository userRepository;

    @Override
    public StepResult<User> execute(QueryContext context) {
        try {
            UserProfileRequest request = context.getRequest();
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
            
            context.put("user", user);
            return StepResult.success(user);
        } catch (UserNotFoundException e) {
            return StepResult.failure("User not found", "USER_NOT_FOUND", ErrorType.BUSINESS);
        } catch (Exception e) {
            return StepResult.systemFailure("Database error occurred");
        }
    }
}
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

## 🔧 Configuration

### **Auto-Configuration**
The framework automatically configures itself when Spring Boot detects it on the classpath:

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    // FlowStep Framework is automatically configured!
}
```

### **Custom Global Exception Handler**
```java
@ControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {
    
    @Override
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        // Custom business exception handling
        logBusinessException(e);
        return super.handleBusinessException(e);
    }
}
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

## 📖 Documentation

- **[Migration Guide](docs/migration-steps.md)** - Complete demo-to-library migration
- **[Library Development Guide](docs/library-development-guide.md)** - Publishing and maintenance
- **[Library Checklist](docs/library-checklist.md)** - Pre-release verification

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### **Development Setup**
```bash
# Clone repository
git clone https://github.com/kayufok/flowstep-framework.git
cd flowstep-framework

# Build and test
./gradlew build

# Run tests
./gradlew test
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- Inspired by enterprise patterns from Eric Evans' Domain-Driven Design
- CQRS concepts from Greg Young and Udi Dahan  
- Template Method pattern from Gang of Four Design Patterns
- Spring Boot team for excellent framework foundation

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/kayufok/flowstep-framework/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kayufok/flowstep-framework/discussions)  

---

**⭐ If this framework helps your enterprise development, please consider giving it a star!**

*Made with ❤️ for enterprise Java developers*
