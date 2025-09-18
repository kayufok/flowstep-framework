# 🔄 FlowStep

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep-framework/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep-framework)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/[USERNAME]/flowstep-framework/workflows/CI/badge.svg)](https://github.com/[USERNAME]/flowstep-framework/actions)
[![codecov](https://codecov.io/gh/[USERNAME]/flowstep-framework/branch/main/graph/badge.svg)](https://codecov.io/gh/[USERNAME]/flowstep-framework)

> **Clean CQRS framework for Spring Boot applications of any size - from prototype to production.**

## 🎯 Why FlowStep?

Modern applications need **clean architecture**, **testable code**, and **maintainable business logic**. FlowStep provides:

- 🔥 **CQRS Pattern**: Clean separation between read (Query) and write (Command) operations
- 🏗️ **Template Method**: Enforced execution flow prevents architectural drift
- 🔧 **Step-Based Design**: Highly testable, modular components
- 📊 **Built-in Auditing**: Automatic tracking of user actions and system events
- 🛡️ **Type-Safe Error Handling**: Comprehensive error classification and management
- ⚡ **Spring Boot Integration**: Seamless auto-configuration and dependency injection

## 🚀 Quick Start

### Maven
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'net.xrftech:flowstep-framework:1.0.0'
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

## 🏢 FlowStep Features

### **Built-in Auditing**
```java
// Automatic audit trail for all commands
@Override
protected void initializeContext(CommandContext context, CreateOrderCommand command) {
    context.setUserId(getCurrentUserId());
    context.setSource("WEB_APPLICATION");
    context.addAuditInfo(Map.of(
        "ipAddress", getClientIpAddress(),
        "sessionId", getSessionId()
    ));
}
```

### **Event Publishing**
```java
@Override
public StepResult<Order> execute(CommandContext context) {
    Order order = createOrder(context);
    
    // Add events for publication after successful transaction
    context.addEvent(new OrderCreatedEvent(order.getId()));
    context.addEvent(new InventoryReservedEvent(order.getItems()));
    
    return StepResult.success(order);
}
```

### **Architectural Governance**
```java
// Enforce architectural rules with ArchUnit
@ArchTest
static final ArchRule enquiry_services_must_be_annotated = 
    classes().that().areAssignableTo(QueryTemplate.class)
             .should().beAnnotatedWith(QueryFlow.class);

@ArchTest  
static final ArchRule command_services_must_be_transactional =
    classes().that().areAnnotatedWith(CommandFlow.class)
             .should().beAnnotatedWith(Transactional.class);
```

## 🎮 Advanced Usage

### **Conditional Step Execution**
```java
@Override
protected List<CommandStep<?>> steps(ProcessOrderCommand command, CommandContext context) {
    List<CommandStep<?>> steps = new ArrayList<>();
    
    steps.add(validateOrderStep);
    
    if (command.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
        steps.add(processCreditCardStep);
    } else if (command.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
        steps.add(processBankTransferStep);
    }
    
    steps.add(finalizeOrderStep);
    return steps;
}
```

### **Cross-Step Data Flow**
```java
// Step 1: Calculate pricing
public StepResult<PricingInfo> execute(CommandContext context) {
    PricingInfo pricing = calculatePricing(context.getCommand());
    context.put("pricing", pricing);
    context.put("discountApplied", pricing.getDiscountPercent() > 0);
    return StepResult.success(pricing);
}

// Step 2: Apply conditional logic based on previous step
public StepResult<Void> execute(CommandContext context) {
    if (context.getOrDefault("discountApplied", false)) {
        sendDiscountNotification(context);
    }
    return StepResult.success();
}
```

### **Custom Validation**
```java
@Override
protected StepResult<Void> validate(CreateUserCommand command) {
    if (StringUtils.isBlank(command.getEmail())) {
        return StepResult.validationFailure("Email is required");
    }
    
    if (!EmailValidator.isValid(command.getEmail())) {
        return StepResult.validationFailure("Invalid email format");
    }
    
    return StepResult.success();
}
```

## 🔧 Configuration

### **Auto-Configuration**
The framework automatically configures itself when Spring Boot detects it on the classpath:

```java
@Configuration
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    // Framework is automatically configured!
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

## 📊 Benefits for FlowStep Teams

| Challenge | Solution |
|-----------|----------|
| **Inconsistent Code Patterns** | Template method enforces uniform structure |
| **Difficult Testing** | Step-based design enables focused unit tests |
| **Poor Audit Trails** | Built-in context tracking and event publishing |
| **Architectural Drift** | ArchUnit rules prevent pattern violations |
| **Complex Error Handling** | Typed error system with automatic HTTP mapping |
| **Team Onboarding** | Clear patterns reduce learning curve |
| **Code Review Overhead** | Standardized structure speeds reviews |
| **Maintenance Burden** | Modular steps are easy to modify independently |

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐
│   Controller    │    │   Controller    │
│    (HTTP)       │    │    (HTTP)       │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│ QueryTemplate │    │ CommandTemplate │
│   (Read-Only)   │    │ (Transactional) │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│  QueryStep    │    │  CommandStep    │
│  QueryStep    │    │  CommandStep    │
│  QueryStep    │    │  CommandStep    │
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
    @Transactional
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

- **[Getting Started Guide](docs/getting-started.md)** - Detailed setup and first steps
- **[API Reference](docs/api-reference.md)** - Complete API documentation  
- **[Best Practices](docs/best-practices.md)** - Recommended patterns and conventions
- **[Migration Guide](docs/migration-guide.md)** - Migrating from other patterns
- **[Examples Repository](https://github.com/[USERNAME]/enterprise-template-examples)** - Complete sample applications

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### **Development Setup**
```bash
# Clone repository
git clone https://github.com/[USERNAME]/flowstep-framework.git
cd flowstep-framework

# Build and test
./gradlew build

# Run architecture tests  
./gradlew test --tests "*ArchitectureTest*"
```

### **Submitting Changes**
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- Inspired by enterprise patterns from Eric Evans' Domain-Driven Design
- CQRS concepts from Greg Young and Udi Dahan  
- Template Method pattern from Gang of Four Design Patterns
- Spring Boot team for excellent framework foundation

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/[USERNAME]/flowstep-framework/issues)
- **Discussions**: [GitHub Discussions](https://github.com/[USERNAME]/flowstep-framework/discussions)  
- **Email**: [your-email@example.com](mailto:your-email@example.com)

---

**⭐ If this framework helps your enterprise development, please consider giving it a star!**

*Made with ❤️ for enterprise Java developers*
