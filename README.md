# 🔄 FlowStep Spring Boot Starter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.xrftech/flowstep-spring-boot-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://github.com/kayufok/flowstep-framework/workflows/CI/badge.svg)](https://github.com/kayufok/flowstep-framework/actions)

> **Clean CQRS Spring Boot Starter implementing Template Method pattern for maintainable, step-based business logic execution.**

## 🎯 Why FlowStep?

Modern Spring Boot applications need **clean architecture**, **testable code**, and **maintainable business logic**. FlowStep provides:

- 🔥 **CQRS Pattern**: Clean separation between read (Query) and write (Command) operations
- 🏗️ **Template Method**: Enforced 4-step execution flow prevents architectural drift
- 🔧 **Step-Based Design**: Highly testable, modular components with context-based communication
- 🛡️ **Type-Safe Error Handling**: 3-tier error classification (Validation, Business, System)
- ⚡ **Spring Boot Integration**: Zero-configuration auto-setup with global exception handling
- 🎯 **Multi-Version Support**: Dual starters for Spring Boot 2.7.x+ (Java 8+) and 3.x+ (Java 17+)
- 📊 **Enterprise Ready**: Transaction management, audit trails, and event publishing support

## 📦 Choose Your Version

FlowStep provides two starter modules to support different environments:

| Starter | Java Version | Spring Boot Version | Key Dependencies | Use Case |
|---------|-------------|-------------------|------------------|----------|
| **flowstep-spring-boot-2-starter** | Java 8+ | 2.7.18+ | Spring 5.3.x, javax.validation | Legacy applications, Java 8 environments |
| **flowstep-spring-boot-3-starter** | Java 17+ | 3.2.1+ | Spring 6.1.x, jakarta.validation | Modern applications, latest features |

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
@QueryFlow(code = "USER_ORDER_SUMMARY", desc = "Retrieve user order summary")
@Service
public class UserOrderSummaryQueryService extends QueryTemplate<UserOrderSummaryRequest, UserOrderSummaryResponse> {

    private final FetchUserStep fetchUserStep;
    private final FetchUserOrdersStep fetchUserOrdersStep;
    private final CalculateOrderStatisticsStep calculateOrderStatisticsStep;

    @Override
    protected StepResult<Void> validate(UserOrderSummaryRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            return StepResult.failure("VAL_001", "User ID must be positive", ErrorType.VALIDATION);
        }
        return StepResult.success();
    }

    @Override
    protected List<QueryStep<?>> steps(UserOrderSummaryRequest request, QueryContext context) {
        return Arrays.asList(
            fetchUserStep,              // Step 1: Fetch user info
            fetchUserOrdersStep,        // Step 2: Fetch user orders (depends on user)
            calculateOrderStatisticsStep // Step 3: Calculate statistics
        );
    }

    @Override
    protected UserOrderSummaryResponse buildResponse(QueryContext context) {
        User user = context.get("user", User.class);
        List<Order> userOrders = context.get("userOrders");
        Map<String, Object> statistics = context.get("orderStatistics");
        
        return UserOrderSummaryResponse.builder()
            .user(user)
            .recentOrders(userOrders)
            .totalSpent((BigDecimal) statistics.get("totalSpent"))
            .totalOrderCount((Integer) statistics.get("totalOrderCount"))
            .build();
    }
}
```

### Step 3: Create Your First Command
```java
@CommandFlow(code = "CREATE_ORDER", desc = "Process new customer order")
@Service
@Transactional  // Required for CommandTemplate operations
public class CreateOrderCommandService extends CommandTemplate<CreateOrderCommand, CreateOrderResponse> {

    private final ValidateUserStep validateUserStep;
    private final ValidateProductsAndStockStep validateProductsAndStockStep;
    private final CreateOrderStep createOrderStep;
    private final CreateOrderItemsAndUpdateStockStep createOrderItemsAndUpdateStockStep;

    @Override
    protected StepResult<Void> validate(CreateOrderCommand command) {
        if (command.getUserId() == null || command.getUserId() <= 0) {
            return StepResult.failure("VAL_001", "User ID must be positive", ErrorType.VALIDATION);
        }
        
        if (command.getOrderItems() == null || command.getOrderItems().isEmpty()) {
            return StepResult.failure("VAL_002", "Order items are required", ErrorType.VALIDATION);
        }
        
        return StepResult.success();
    }

    @Override
    protected List<CommandStep<?>> steps(CreateOrderCommand command, CommandContext context) {
        return Arrays.asList(
            validateUserStep,                    // Step 1: Validate user
            validateProductsAndStockStep,        // Step 2: Validate products and stock
            createOrderStep,                     // Step 3: Create order
            createOrderItemsAndUpdateStockStep   // Step 4: Create order items and update stock
        );
    }

    @Override
    protected CreateOrderResponse buildResponse(CommandContext context) {
        Order createdOrder = context.get("createdOrder", Order.class);
        List<OrderItem> createdOrderItems = context.get("createdOrderItems");
        
        return CreateOrderResponse.builder()
            .order(createdOrder)
            .orderItems(createdOrderItems)
            .message("Order created successfully with " + createdOrderItems.size() + " items")
            .build();
    }

    @Override
    protected void handlePostExecution(CommandContext context) {
        // Publish domain events, send notifications, etc.
        List<Object> events = context.getEvents();
        log.info("Order creation completed with {} audit events", events.size());
        super.handlePostExecution(context);
    }
}
```

### Step 4: Use in Your Controller
```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final UserOrderSummaryQueryService userOrderSummaryQuery;
    private final CreateOrderCommandService createOrderCommand;

    @GetMapping("/users/{userId}/summary")
    public UserOrderSummaryResponse getUserOrderSummary(
            @PathVariable Long userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) throws BusinessException {
        
        var request = UserOrderSummaryRequest.builder()
            .userId(userId)
            .startDate(startDate)
            .endDate(endDate)
            .includeTopProducts(true)
            .build();
            
        return userOrderSummaryQuery.execute(request);
    }

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody CreateOrderCommand command) throws BusinessException {
        return createOrderCommand.execute(command);
    }
}
```

That's it! FlowStep automatically configures itself and provides global exception handling.

## 🔧 Key Components

### **Core Framework Classes**
```
net.xrftech.flowstep/
├── QueryTemplate<R,S>              # Abstract base for read operations
├── CommandTemplate<C,R>            # Abstract base for write operations  
├── context/
│   ├── BaseContext                 # Common context functionality
│   ├── QueryContext               # Query-specific context with request data
│   └── CommandContext             # Command-specific context with audit info
├── step/
│   ├── QueryStep<T>               # Functional interface for query steps
│   ├── CommandStep<T>             # Functional interface for command steps
│   └── StepResult<T>              # Type-safe step execution result
├── exception/
│   ├── BusinessException          # Framework business exception
│   ├── ErrorType                  # Error classification enum
│   ├── ErrorResponse              # REST error response DTO
│   └── GlobalExceptionHandler     # Spring Boot global exception handler
├── annotation/
│   ├── @QueryFlow                 # Query service documentation annotation
│   └── @CommandFlow               # Command service documentation annotation
└── config/
    ├── FlowStepAutoConfiguration  # Spring Boot auto-configuration
    └── FlowStepProperties         # Configuration properties
```

### **Auto-Configuration Features**
- **Zero Configuration**: Works out-of-the-box with Spring Boot
- **Global Exception Handler**: Automatic REST error response formatting
- **Conditional Beans**: Smart bean creation based on classpath detection
- **Property Binding**: Externalized configuration via `flowstep.*` properties

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
Every service follows a consistent 4-step execution flow:
1. **Validation** - Request/command validation with StepResult
2. **Context Initialization** - Shared data setup with audit information
3. **Step Execution** - Sequential business logic steps with context communication
4. **Response Building** - Final response aggregation from context data

**Additional for Commands:**
- **Post-Execution** - Event publishing and audit handling

### **CQRS Separation**
- **QueryTemplate**: Read-only operations, no transactions, immutable context sharing
- **CommandTemplate**: Write operations, requires @Transactional, event publishing support

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
context.put("user", user);
context.put("createdOrder", savedOrder);

// Retrieve data from previous steps (type-safe)
User user = context.get("user", User.class);
List<OrderItem> items = context.get("createdOrderItems");

// Query context includes request access
QueryContext queryContext = new QueryContext();
queryContext.setRequest(request);
queryContext.markStartTime();

// Command context includes audit info
CommandContext commandContext = new CommandContext();
commandContext.setCommand(command);
commandContext.setTimestamp(LocalDateTime.now());
commandContext.addEvent(new OrderCreatedEvent(order.getId()));
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        FlowStep Framework                           │
├─────────────────────────────────────────────────────────────────────┤
│  @RestController                                                    │
│  ├── QueryService.execute(request) → Response                       │
│  └── CommandService.execute(command) → Response                     │
└─────────────────────┬───────────────────┬───────────────────────────┘
                      │                   │
          ┌───────────▼──────────┐    ┌───▼──────────────────┐
          │   QueryTemplate      │    │   CommandTemplate    │
          │   (Read Operations)  │    │  (Write Operations)  │
          └───────────┬──────────┘    └───┬──────────────────┘
                      │                   │
          ┌───────────▼──────────┐    ┌───▼──────────────────┐
          │ 1. validate()        │    │ 1. validate()        │
          │ 2. Context Setup     │    │ 2. initializeContext()│
          │ 3. steps() execution │    │ 3. steps() execution │
          │ 4. buildResponse()   │    │ 4. buildResponse()   │
          │                      │    │ 5. handlePostExecution()│
          └───────────┬──────────┘    └───┬──────────────────┘
                      │                   │
          ┌───────────▼──────────┐    ┌───▼──────────────────┐
          │   QueryStep<T>       │    │   CommandStep<T>     │
          │   QueryStep<T>       │    │   CommandStep<T>     │
          │   QueryStep<T>       │    │   CommandStep<T>     │
          │                      │    │                      │
          │ Context: QueryContext│    │ Context: CommandContext│
          │ - Request data       │    │ - Command data       │
          │ - Shared state       │    │ - Audit info         │
          │ - Execution timing   │    │ - Event publishing   │
          └──────────────────────┘    └──────────────────────┘
```

## 🧪 Testing

### **Unit Testing Steps**
```java
class FetchUserStepTest {
    
    @MockBean
    private UserRepository userRepository;
    
    @Test
    void shouldFetchUserSuccessfully() {
        // Given
        QueryContext context = new QueryContext();
        context.setRequest(UserOrderSummaryRequest.builder().userId(123L).build());
        
        User mockUser = new User(123L, "John Doe", "john@example.com", User.UserStatus.ACTIVE);
        when(userRepository.findById(123L)).thenReturn(Optional.of(mockUser));
        
        // When
        StepResult<User> result = fetchUserStep.execute(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.get("user", User.class)).isEqualTo(mockUser);
        assertThat(context.get("user", User.class).getStatus()).isEqualTo(User.UserStatus.ACTIVE);
    }
    
    @Test
    void shouldFailWhenUserNotFound() {
        // Given
        QueryContext context = new QueryContext();
        context.setRequest(UserOrderSummaryRequest.builder().userId(999L).build());
        
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        StepResult<User> result = fetchUserStep.execute(context);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorType()).isEqualTo(ErrorType.BUSINESS);
        assertThat(result.getErrorCode()).isEqualTo("USR_001");
    }
}
```

### **Integration Testing Services**
```java
@SpringBootTest
@Transactional
class CreateOrderCommandServiceTest {
    
    @Autowired
    private CreateOrderCommandService createOrderService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given - Create test data
        User user = new User("John Doe", "john@example.com", User.UserStatus.ACTIVE);
        entityManager.persistAndFlush(user);
        
        CreateOrderCommand command = CreateOrderCommand.builder()
            .userId(user.getId())
            .orderItems(List.of(
                CreateOrderCommand.OrderItem.builder()
                    .productId(1L)
                    .quantity(2)
                    .build()
            ))
            .build();
        
        // When
        CreateOrderResponse response = createOrderService.execute(command);
        
        // Then
        assertThat(response.getOrder()).isNotNull();
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getMessage()).contains("Order created successfully");
        
        // Verify database state
        Order savedOrder = entityManager.find(Order.class, response.getOrder().getId());
        assertThat(savedOrder.getUserId()).isEqualTo(user.getId());
        assertThat(savedOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }
}
```

### **Optional Architecture Testing**
FlowStep includes optional ArchUnit support for architecture validation:

```bash
# Enable architecture tests (disabled by default)
./gradlew test -PenableArchUnit=true

# Test specific module
./gradlew :flowstep-spring-boot-3-starter:test -PenableArchUnit=true
```

ArchUnit is included as a test dependency but tests are conditionally enabled:
```java
@EnabledIf("#{systemProperties['flowstep.archunit.enabled'] == 'true'}")
class ArchitectureTest {
    
    @Test
    void queryServicesShouldExtendQueryTemplate() {
        classes().that().areAnnotatedWith(QueryFlow.class)
            .should().beAssignableTo(QueryTemplate.class);
    }
    
    @Test 
    void commandServicesShouldExtendCommandTemplate() {
        classes().that().areAnnotatedWith(CommandFlow.class)
            .should().beAssignableTo(CommandTemplate.class);
    }
}
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
git clone https://github.com/kayufok/flowstep-framework.git
cd flowstep-framework

# Build all modules (parent + 2 starters)
./gradlew build

# Test specific modules
./gradlew :flowstep-spring-boot-2-starter:test
./gradlew :flowstep-spring-boot-3-starter:test

# Run with architecture tests (optional)
./gradlew test -PenableArchUnit=true

# Print project information
./gradlew printVersion
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- Inspired by enterprise patterns from Eric Evans' Domain-Driven Design
- CQRS concepts from Greg Young and Udi Dahan  
- Template Method pattern from Gang of Four Design Patterns
- Spring Boot team for excellent framework foundation
- Lombok project for reducing boilerplate code
- ArchUnit for architecture testing capabilities

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/kayufok/flowstep-framework/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kayufok/flowstep-framework/discussions)  

## 🔗 Project Structure

```
flowstep-framework/
├── flowstep-spring-boot-2-starter/    # Java 8+ & Spring Boot 2.7.x support
├── flowstep-spring-boot-3-starter/    # Java 17+ & Spring Boot 3.x support  
├── docs/                              # Complete documentation
│   ├── API_REFERENCE.md
│   ├── ARCHITECTURE.md
│   ├── USAGE_GUIDE.md
│   ├── TESTING_GUIDE.md
│   └── CONFIGURATION_REFERENCE.md
└── summaries/                         # Development summaries
```

## 🔗 Related Projects

- **Documentation**: Complete documentation available in `/docs`

---

**⭐ If this starter helps your Spring Boot development, please consider giving it a star!**

*Made with ❤️ for Spring Boot developers*