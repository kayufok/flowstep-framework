# 📚 FlowStep API Reference

## Core Classes

### 🎯 QueryTemplate<R, S>

**Package:** `net.xrftech.flowstep`

Abstract template class for implementing query (read) operations following CQRS pattern.

#### Type Parameters
- `R` - Request type for the query
- `S` - Response type for the query

#### Key Methods

##### `execute(R request) : S`
Main execution method that orchestrates the query flow.

**Execution Flow:**
1. Validates request via `validate()`
2. Creates and initializes `QueryContext`
3. Executes all steps from `steps()` in sequence
4. Builds response via `buildResponse()`

**Throws:** `BusinessException` if validation or any step fails

##### `validate(R request) : StepResult<Void>`
*Protected, Optional Override*

Validates the query request. Default implementation returns success.

**Parameters:**
- `request` - The query request to validate

**Returns:** `StepResult` indicating validation success/failure

##### `steps(R request, QueryContext context) : List<QueryStep<?>>`
*Protected, Abstract*

Defines the sequence of steps to execute for this query.

**Parameters:**
- `request` - The query request
- `context` - Query context containing request and shared state

**Returns:** Ordered list of steps to execute

##### `buildResponse(QueryContext context) : S`
*Protected, Abstract*

Builds the final response from the context after all steps complete.

**Parameters:**
- `context` - Query context containing step results

**Returns:** The query response

#### Example Implementation

```java
@QueryFlow(code = "USER_PROFILE", desc = "Fetch user profile")
@Service
public class UserProfileQuery extends QueryTemplate<UserRequest, UserResponse> {
    
    @Override
    protected List<QueryStep<?>> steps(UserRequest request, QueryContext context) {
        return List.of(
            () -> {
                User user = userRepository.findById(request.getId());
                context.put("user", user);
                return StepResult.success(user);
            },
            () -> {
                User user = context.get("user");
                List<Preference> prefs = prefRepository.findByUserId(user.getId());
                context.put("preferences", prefs);
                return StepResult.success(prefs);
            }
        );
    }
    
    @Override
    protected UserResponse buildResponse(QueryContext context) {
        return UserResponse.builder()
            .user(context.get("user"))
            .preferences(context.get("preferences"))
            .build();
    }
}
```

---

### 🔧 CommandTemplate<C, R>

**Package:** `net.xrftech.flowstep`

Abstract template class for implementing command (write) operations following CQRS pattern.

#### Type Parameters
- `C` - Command type
- `R` - Response type

#### Key Methods

##### `execute(C command) : R`
Main execution method that orchestrates the command flow.

**Execution Flow:**
1. Validates command via `validate()`
2. Creates and initializes `CommandContext` with audit info
3. Calls `initializeContext()` for custom initialization
4. Executes all steps from `steps()` in sequence
5. Builds response via `buildResponse()`
6. Handles post-execution via `handlePostExecution()`

**Throws:** `BusinessException` if validation or any step fails

##### `validate(C command) : StepResult<Void>`
*Protected, Optional Override*

Validates the command. Default implementation returns success.

##### `initializeContext(CommandContext context, C command) : void`
*Protected, Optional Override*

Initializes context with additional information (user, permissions, etc).

##### `steps(C command, CommandContext context) : List<CommandStep<?>>`
*Protected, Abstract*

Defines the sequence of steps to execute for this command.

##### `buildResponse(CommandContext context) : R`
*Protected, Abstract*

Builds the command response from the context.

##### `handlePostExecution(CommandContext context) : void`
*Protected, Optional Override*

Handles post-execution tasks like event publishing.

#### Example Implementation

```java
@CommandFlow(code = "CREATE_ORDER", desc = "Create new order")
@Service
@Transactional
public class CreateOrderCommand extends CommandTemplate<OrderRequest, OrderResponse> {
    
    @Override
    protected void initializeContext(CommandContext context, OrderRequest command) {
        context.setUserId(SecurityContext.getCurrentUserId());
        context.setTimestamp(LocalDateTime.now());
    }
    
    @Override
    protected List<CommandStep<?>> steps(OrderRequest command, CommandContext context) {
        return List.of(
            () -> {
                // Validate inventory
                boolean available = inventoryService.checkAvailability(command.getItems());
                if (!available) {
                    return StepResult.failure("Insufficient inventory", "INV_001", ErrorType.BUSINESS);
                }
                return StepResult.success();
            },
            () -> {
                // Create order
                Order order = new Order(command);
                Order saved = orderRepository.save(order);
                context.put("order", saved);
                context.addEvent(new OrderCreatedEvent(saved));
                return StepResult.success(saved);
            }
        );
    }
    
    @Override
    protected OrderResponse buildResponse(CommandContext context) {
        Order order = context.get("order");
        return new OrderResponse(order.getId(), order.getOrderNumber());
    }
    
    @Override
    protected void handlePostExecution(CommandContext context) {
        context.getEvents().forEach(eventPublisher::publish);
    }
}
```

---

## Context Classes

### 📦 BaseContext

**Package:** `net.xrftech.flowstep.context`

Base class providing shared storage mechanism for step execution.

#### Key Methods

| Method | Description |
|--------|-------------|
| `put(String key, T value)` | Stores a value in the context |
| `get(String key) : T` | Retrieves a value by key |
| `has(String key) : boolean` | Checks if key exists |
| `getOrDefault(String key, T default) : T` | Gets value or returns default |
| `remove(String key) : T` | Removes and returns value |
| `clear()` | Clears all values |
| `size() : int` | Returns number of entries |
| `isEmpty() : boolean` | Checks if context is empty |

### 📖 QueryContext

**Package:** `net.xrftech.flowstep.context`

Context for query operations, extends BaseContext.

#### Additional Features
- Request storage
- Execution time tracking
- Read-only operation context

### ✏️ CommandContext

**Package:** `net.xrftech.flowstep.context`

Context for command operations, extends BaseContext.

#### Additional Features
- Command storage
- Timestamp tracking
- Event collection
- Audit information
- User context support

#### Key Methods

| Method | Description |
|--------|-------------|
| `setCommand(C command)` | Sets the command being executed |
| `setTimestamp(LocalDateTime time)` | Sets execution timestamp |
| `setUserId(String userId)` | Sets user ID for audit |
| `addEvent(Object event)` | Adds domain event |
| `getEvents() : List<Object>` | Gets all events |
| `getAuditInfo() : Map<String, Object>` | Gets audit information |

---

## Step Interfaces

### 🚶 QueryStep<T>

**Package:** `net.xrftech.flowstep.step`

Functional interface for query step execution.

```java
@FunctionalInterface
public interface QueryStep<T> {
    StepResult<T> execute(QueryContext context);
}
```

### 🏃 CommandStep<T>

**Package:** `net.xrftech.flowstep.step`

Functional interface for command step execution.

```java
@FunctionalInterface
public interface CommandStep<T> {
    StepResult<T> execute(CommandContext context);
}
```

---

## Result Classes

### ✅ StepResult<T>

**Package:** `net.xrftech.flowstep.step`

Result wrapper for step execution outcomes.

#### Properties
- `success: boolean` - Success indicator
- `data: T` - Result data (if successful)
- `message: String` - Error message (if failed)
- `errorCode: String` - Error code (if failed)
- `errorType: ErrorType` - Error classification (if failed)

#### Factory Methods

| Method | Description | Usage |
|--------|-------------|-------|
| `success(T data)` | Creates success with data | `return StepResult.success(user);` |
| `success()` | Creates success without data | `return StepResult.success();` |
| `failure(String message, String code, ErrorType type)` | Creates failure with details | `return StepResult.failure("Not found", "USR_404", ErrorType.BUSINESS);` |
| `failure(String message)` | Quick failure (BUSINESS type) | `return StepResult.failure("Invalid request");` |
| `validationFailure(String message)` | Validation error | `return StepResult.validationFailure("Email required");` |
| `systemFailure(String message)` | System error | `return StepResult.systemFailure("Database unavailable");` |

---

## Exception Classes

### 🚨 BusinessException

**Package:** `net.xrftech.flowstep.exception`

Business exception for handling expected business errors.

#### Constructors

```java
// Full constructor
new BusinessException(String errorCode, String message, ErrorType errorType)

// Convenience constructor (defaults to BUSINESS type)
new BusinessException(String errorCode, String message)
```

#### Properties
- `errorCode: String` - Unique error identifier
- `message: String` - Human-readable message
- `errorType: ErrorType` - Error classification

### 🏷️ ErrorType

**Package:** `net.xrftech.flowstep.exception`

Enum for error classification.

```java
public enum ErrorType {
    VALIDATION,  // Client-side fixable (400)
    BUSINESS,    // Business rule violations (409)
    SYSTEM       // Technical failures (500)
}
```

### 📋 ErrorResponse

**Package:** `net.xrftech.flowstep.exception`

Standard error response structure for REST APIs.

```java
{
    "errorCode": "USR_404",
    "message": "User not found",
    "timestamp": "2025-09-12T10:15:30",
    "path": "/api/users/123",
    "details": {}  // Optional additional details
}
```

### 🌐 GlobalExceptionHandler

**Package:** `net.xrftech.flowstep.exception`

Spring MVC exception handler for consistent error responses.

**Handles:**
- `BusinessException` → Appropriate HTTP status based on ErrorType
- `MethodArgumentNotValidException` → 400 Bad Request
- `Exception` → 500 Internal Server Error

---

## Annotations

### 📖 @QueryFlow

**Package:** `net.xrftech.flowstep.annotation`

Marks query services for architectural governance.

```java
@QueryFlow(
    code = "USER_PROFILE",  // Unique identifier
    desc = "Fetch user profile with preferences"  // Description
)
public class UserProfileQuery extends QueryTemplate<...> {
    // Implementation
}
```

### ✏️ @CommandFlow

**Package:** `net.xrftech.flowstep.annotation`

Marks command services for architectural governance.

```java
@CommandFlow(
    code = "CREATE_ORDER",  // Unique identifier
    desc = "Process new customer order"  // Description
)
@Transactional  // Required for commands
public class CreateOrderCommand extends CommandTemplate<...> {
    // Implementation
}
```

---

## Configuration Classes

### ⚙️ FlowStepAutoConfiguration

**Package:** `net.xrftech.flowstep.config`

Spring Boot auto-configuration class.

**Features:**
- Conditional on `flowstep.enabled=true` (default)
- Auto-configures `GlobalExceptionHandler` when Spring Web present
- Respects property configuration

### 🎛️ FlowStepProperties

**Package:** `net.xrftech.flowstep.config`

Configuration properties for FlowStep.

```properties
# Enable/disable FlowStep
flowstep.enabled=true

# Enable/disable global exception handler
flowstep.exception-handler.enabled=true

# Include stack traces in error responses
flowstep.exception-handler.include-stack-trace=false
```

---

## Usage Patterns

### Query Pattern
```java
// 1. Define request/response
record UserRequest(Long id) {}
record UserResponse(User user, List<Preference> preferences) {}

// 2. Implement QueryTemplate
@QueryFlow(code = "USER_QUERY", desc = "Get user details")
@Service
public class UserQuery extends QueryTemplate<UserRequest, UserResponse> {
    // Implementation
}

// 3. Use in controller
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) throws BusinessException {
    return userQuery.execute(new UserRequest(id));
}
```

### Command Pattern
```java
// 1. Define command/response
record CreateUserCommand(String name, String email) {}
record CreateUserResponse(Long id, String username) {}

// 2. Implement CommandTemplate
@CommandFlow(code = "CREATE_USER", desc = "Create new user")
@Service
@Transactional
public class CreateUserService extends CommandTemplate<CreateUserCommand, CreateUserResponse> {
    // Implementation
}

// 3. Use in controller
@PostMapping("/users")
public CreateUserResponse createUser(@RequestBody @Valid CreateUserCommand command) 
    throws BusinessException {
    return createUserService.execute(command);
}
```

### Step Composition
```java
// Inline steps
List.of(
    () -> validateStep(context),
    () -> fetchDataStep(context),
    () -> transformStep(context)
)

// Method reference steps
List.of(
    this::validateUser,
    this::checkPermissions,
    this::performAction
)

// Conditional steps
List<CommandStep<?>> steps = new ArrayList<>();
steps.add(this::validateBase);
if (command.requiresApproval()) {
    steps.add(this::requestApproval);
}
steps.add(this::executeMain);
return steps;
```

### Context Usage
```java
// Store and retrieve data
context.put("user", user);
User user = context.get("user");

// Check existence
if (context.has("approvalRequired")) {
    // Handle approval flow
}

// Use defaults
int retries = context.getOrDefault("retries", 3);

// Event handling (CommandContext)
context.addEvent(new UserCreatedEvent(user));
List<Object> events = context.getEvents();
```

### Error Handling
```java
// In steps
if (user == null) {
    return StepResult.failure("User not found", "USR_404", ErrorType.BUSINESS);
}

// Validation failures
if (!isValid(email)) {
    return StepResult.validationFailure("Invalid email format");
}

// System failures
try {
    // External service call
} catch (Exception e) {
    return StepResult.systemFailure("External service unavailable");
}
```

---

## Best Practices

### 1. Step Design
- Keep steps focused and single-purpose
- Use descriptive step names/methods
- Handle errors gracefully with appropriate ErrorType
- Store intermediate results in context

### 2. Context Management
- Use meaningful keys for context storage
- Clean up large objects when no longer needed
- Don't store sensitive data in context
- Prefer typed getters over casting

### 3. Error Handling
- Use specific error codes for different failures
- Provide helpful error messages
- Choose appropriate ErrorType for HTTP mapping
- Log errors at appropriate levels

### 4. Transaction Management
- Always use `@Transactional` on CommandTemplate implementations
- Keep transactions as short as possible
- Consider read-only transactions for complex queries
- Handle rollback scenarios properly

### 5. Testing
- Test steps independently
- Mock context for unit tests
- Use integration tests for full flow validation
- Verify error scenarios

---

## Module Differences

### Spring Boot 2.x Module
- Uses `javax.validation` annotations
- Compatible with Spring Framework 5.x
- Uses `spring.factories` for auto-configuration
- Java 8+ compatible

### Spring Boot 3.x Module
- Uses `jakarta.validation` annotations
- Compatible with Spring Framework 6.x
- Uses `AutoConfiguration.imports` for auto-configuration
- Requires Java 17+

---

## Migration Guide

### From Plain Services to FlowStep

```java
// Before: Traditional service
@Service
public class UserService {
    public UserDto getUser(Long id) {
        User user = userRepository.findById(id);
        List<Preference> prefs = prefRepository.findByUserId(id);
        return new UserDto(user, prefs);
    }
}

// After: FlowStep query
@QueryFlow(code = "USER_QUERY", desc = "Get user with preferences")
@Service
public class UserQuery extends QueryTemplate<Long, UserDto> {
    @Override
    protected List<QueryStep<?>> steps(Long id, QueryContext context) {
        return List.of(
            () -> {
                User user = userRepository.findById(id);
                context.put("user", user);
                return StepResult.success(user);
            },
            () -> {
                List<Preference> prefs = prefRepository.findByUserId(id);
                context.put("preferences", prefs);
                return StepResult.success(prefs);
            }
        );
    }
    
    @Override
    protected UserDto buildResponse(QueryContext context) {
        return new UserDto(context.get("user"), context.get("preferences"));
    }
}
```

### Benefits of Migration
- ✅ Clear separation of concerns
- ✅ Consistent error handling
- ✅ Better testability
- ✅ Audit trail support
- ✅ Event-driven capabilities
- ✅ Enforced architectural patterns