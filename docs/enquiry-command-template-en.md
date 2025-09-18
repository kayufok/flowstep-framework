### **🔄 FlowStep Framework Design Document (Final Complete Version)**
#### **Spring Boot-Based "Query-Command Separation" Framework Architecture**
#### **Unified Design Language Supporting Query (R) and Command (CUD)**

---

### **🎯 Objectives**

Establish a unified, scalable, process-enforcing, highly testable, and maintainable enterprise-level service development standard that clearly separates "query" and "write" operations, achieving:

| Objective | Implementation Method |
| :--- | :--- |
| ✅ Query-Command Separation | Distinguish between `QueryTemplate` and `CommandTemplate` |
| ✅ Process Enforcement | Template method pattern defining skeletal workflow |
| ✅ Pure Results | Return pure business objects, unbound from HTTP layer |
| ✅ Context Sharing | `BaseContext` supports cross-step data transfer |
| ✅ Easy Extension | New services only need to inherit templates and implement abstract methods |
| ✅ High Testability | Each Step can be unit tested independently, supports ArchUnit auditing |
| ✅ Architecture Governance | Custom annotations + static analysis prevent architecture erosion |
| ✅ Out-of-the-box | `validate` provides default implementation, simple scenarios need no override |
| ✅ Intuitive Conditions | Direct use of `if-else` in `steps()` method, clear and understandable |

---

### **📁 Overall Project Structure**

```
src/
 └── main/
    └── java/
        └── com.example.demo/
            ├── DemoApplication.java
            │
            └── template/                      ← Core shared component layer
                ├── annotation/               ← Custom annotations
                │   ├── QueryFlow.java      ← Mark query services
                │   └── CommandFlow.java      ← Mark write services
                │
                ├── context/                  ← Context base classes
                │   └── BaseContext.java      ← Shared storage mechanism
                │   ├── QueryContext.java   ← Query-specific context
                │   └── CommandContext.java   ← Write-specific context
                │
                ├── step/                     ← Step interfaces
                │   ├── QueryStep.java      ← Query steps
                │   ├── CommandStep.java      ← Write steps
                │   └── StepResult.java       ← Step result encapsulation
                │
                ├── exception/                ← Shared exceptions
                │   ├── BusinessException.java ← Business exception model
                │   └── ErrorType.java        ← Common error type enum
                │
                ├── QueryTemplate.java      ← Query template
                └── CommandTemplate.java      ← Write template
            │
            ├── domain/
            │   ├── request/                  ← Query requests
            │   │   └── QueryRequest.java
            │   ├── command/                  ← Write commands
            │   │   └── CreateUserCommand.java
            │   ├── response/                 ← Unified response models
            │   │   ├── QueryResponse.java
            │   │   └── CreateResponse.java
            │   └── User.java
            │
            ├── repository/
            │   └── UserMapper.java
            │
            ├── client/
            │   └── ExternalApiClient.java
            │
            ├── validator/
            │   ├── QueryValidator.java     ← Query validation
            │   └── CommandValidator.java     ← Write validation
            │
            ├── service/impl/
            │   ├── CreditQueryService.java ← Query service example
            │   └── CreateUserService.java    ← Write service example
            │
            └── controller/
                ├── QueryController.java
                └── CommandController.java
```

> 💡 **Note**: The `template` package serves as core infrastructure from which all services derive, ensuring unified design language.

---

### **🔧 Core Design Components (Unified Design Language)**

#### **1. `@QueryFlow` & `@CommandFlow` —— Service Marking and Audit Foundation**

```java
// Query service marker
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryFlow {
    String code();
    String desc();
}

// Write service marker
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandFlow {
    String code();
    String desc();
}
```

✅ **Purpose**:
*   Identify all standardized services in the system.
*   Support ArchUnit static auditing, enforcing all enquiry/command services must be annotated accordingly.
*   Provide metadata support for future "service registry", "process visualization", and "operation auditing".

---

#### **2. `ErrorType` —— Common Error Type Enum**

```java
// File location: template/exception/ErrorType.java
package com.example.demo.template.exception;

/**
 * Error type enum for unified error nature identification.
 * Applicable to StepResult and BusinessException.
 */
public enum ErrorType {
    /**
     * Input validation error (e.g., missing parameters, format errors)
     * Usually fixed by client.
     */
    VALIDATION,

    /**
     * Business logic error (e.g., insufficient balance, out of stock)
     * Usually resolved by business rules or user decisions.
     */
    BUSINESS,

    /**
     * System internal error (e.g., database connection failure, null pointer)
     * Requires developer intervention to fix.
     */
    SYSTEM
}
```

✅ **Purpose**:
*   Serves as shared error classification standard for `StepResult` and `BusinessException`.
*   Provides basis for automated HTTP status code mapping in global exception handler (`@ControllerAdvice`).
*   Supports future monitoring and alerting systems for error type aggregation analysis.

---

#### **3. `BaseContext` —— Context Sharing Base Class (Shared)**

```java
public abstract class BaseContext {
    protected final Map<String, Object> store = new HashMap<>();

    public <T> void put(String key, T value) { store.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) store.get(key); }

    public boolean has(String key) { return store.containsKey(key); }

    // New: Get method with default value, improves condition judgment fluency
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        T value = (T) store.get(key);
        return value != null ? value : defaultValue;
    }
}
```

✅ **Features**: Simple, flexible, non-intrusive, providing unified container for passing intermediate results between multiple steps.

---

#### **4. `QueryContext` / `CommandContext` —— Specialized Contexts (Inheritance Extension)**

```java
@Data
@EqualsAndHashCode(callSuper=false)
public class QueryContext extends BaseContext {
    // Extensible: Add query scenario-specific fields (e.g., traceId)
}

@Data
@EqualsAndHashCode(callSuper=false)
public class CommandContext extends BaseContext {
    // Extensible: Add command-specific fields
}
```

✅ **Design Principle**: Inherit `BaseContext`, retain core capabilities, and extend semantic properties based on scenarios.

---

#### **5. `StepResult<T>` —— Step Execution Result Encapsulation (Shared)**

```java
public class StepResult<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;
    private final ErrorType errorType; // <-- Reference common ErrorType

    // Remove internal enum ErrorType definition

    // Static factory methods
    public static <T> StepResult<T> success(T data) {
        return new StepResult<>(true, data, null, null, null);
    }

    public static <T> StepResult<T> failure(String message, String errorCode, ErrorType type) {
        return new StepResult<>(false, null, message, errorCode, type);
    }

    // Simplified version for quick prototyping
    public static <T> StepResult<T> failure(String message) {
        return failure(message, "GENERIC_ERROR", ErrorType.BUSINESS);
    }

    // Private constructor...
    private StepResult(boolean success, T data, String message, String errorCode, ErrorType errorType) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    // getters...
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public ErrorType getErrorType() { return errorType; }
}
```

✅ **Value**: Unified result format, convenient for process judgment and exception translation.

---

#### **6. `BusinessException` —— Business Exception Model (Shared)**

```java
public class BusinessException extends Exception {
    private final String errorCode;
    private final ErrorType errorType; // <-- Reference common ErrorType

    // Remove internal enum ErrorType definition

    public BusinessException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    // getters...
    public String getErrorCode() { return errorCode; }
    public ErrorType getErrorType() { return errorType; }
}
```

✅ **Advantages**: Clear semantics, beneficial for log analysis, monitoring alerts, and frontend processing.

---

#### **7. `QueryStep<T>` / `CommandStep<T>` —— Pluggable Functional Steps (Symmetric Design)**

```java
@FunctionalInterface
public interface QueryStep<T> {
    StepResult<T> execute(QueryContext context) throws Exception;
}

@FunctionalInterface
public interface CommandStep<T> {
    StepResult<T> execute(CommandContext context) throws Exception;
}
```

✅ **Capabilities**: Support DB queries, external API calls, condition judgment, event sending, etc. Highly modular and reusable.

---

#### **8. `QueryTemplate<R, S>` —— Query Process Skeleton**

```java
@Slf4j
public abstract class QueryTemplate<R, S> {

    public final S execute(R request) throws BusinessException {
        try {
            // 1. Validate (use default implementation or override in subclass)
            StepResult<Void> validate = validate(request);
            if (!validate.isSuccess()) {
                throw new BusinessException(validate.getErrorCode(), validate.getMessage(), validate.getErrorType());
            }

            // 2. Create context
            QueryContext context = new QueryContext();
            context.put("request", request);

            // 3. Execute steps (serial)
            for (QueryStep<?> step : steps(request, context)) { // <-- Pass request and context
                @SuppressWarnings("unchecked")
                StepResult<Object> result = ((QueryStep<Object>) step).execute(context);
                if (!result.isSuccess()) {
                    throw new BusinessException(result.getErrorCode(), result.getMessage(), result.getErrorType());
                }
            }

            // 4. Encapsulate and return pure business result
            return buildResponse(context);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in enquiry", e);
            throw new BusinessException("SYS_001", "System error", ErrorType.SYSTEM);
        }
    }

    // Provide default implementation, simple services can skip override
    protected StepResult<Void> validate(R request) {
        return StepResult.success(null);
    }

    // Modified signature, pass request and context for convenient condition judgment
    protected abstract List<QueryStep<?>> steps(R request, QueryContext context);

    protected abstract S buildResponse(QueryContext context);
}
```

✅ **Key**: No transactions, no side effects, return pure query results.

---

#### **9. `CommandTemplate<C, R>` —— Write Process Skeleton (With Transactions and Auditing)**

```java
@Slf4j
public abstract class CommandTemplate<C, R> {

    public final R execute(C command) throws BusinessException {
        try {
            // 1. Validate command (use default implementation or override in subclass)
            StepResult<Void> validate = validate(command);
            if (!validate.isSuccess()) {
                throw new BusinessException(validate.getErrorCode(), validate.getMessage(), validate.getErrorType());
            }

            // 2. Create context
            CommandContext context = new CommandContext();
            context.put("command", command);

            // 3. Execute all steps (serial)
            for (CommandStep<?> step : steps(command, context)) { // <-- Pass command and context
                @SuppressWarnings("unchecked")
                StepResult<Object> result = ((CommandStep<Object>) step).execute(context);
                if (!result.isSuccess()) {
                    throw new BusinessException(result.getErrorCode(), result.getMessage(), result.getErrorType());
                }
            }

            // 4. Encapsulate and return business result
            return buildResponse(context);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in command execution", e);
            throw new BusinessException("SYS_001", "System error", ErrorType.SYSTEM);
        }
    }

    // Provide default implementation
    protected StepResult<Void> validate(C command) {
        return StepResult.success(null);
    }

    // Modified signature
    protected abstract List<CommandStep<?>> steps(C command, CommandContext context);

    protected abstract R buildResponse(CommandContext context);
}
```

✅ **Key**:
*   Must annotate concrete `Service` with `@Transactional` to ensure atomicity.
*   Audit information can be passed between steps through `CommandContext`'s `put/get` methods.

---

### **🛠️ Usage Guide (Developer Guide)**

#### **Query Service Development Steps (Using `CreditQueryService` as Example)**

1.  Define `QueryRequest` and `QueryResponse`
2.  Inherit `QueryTemplate<Req, Resp>`
3.  Inject required `QueryStep`
4.  Implement `steps(request, context)`, `buildResponse` (can skip `validate` if not needed)
5.  Add `@QueryFlow` and `@Service`

```java
@QueryFlow(code = "CREDIT_ENQUIRY", desc = "Credit enquiry service")
@Service
@RequiredArgsConstructor
public class CreditQueryService extends QueryTemplate<QueryRequest, QueryResponse> {
    private final FetchUserFromDbStep fetchUserStep;
    private final FetchCreditFromApiStep fetchCreditStep;

    // validate method can be skipped, uses default implementation

    @Override
    protected List<QueryStep<?>> steps(QueryRequest request, QueryContext context) {
        List<QueryStep<?>> list = new ArrayList<>();
        list.add(fetchUserStep);

        // ✅ Direct use of if-else in steps method, clear and intuitive!
        if (request.isIncludeCredit()) {
            list.add(fetchCreditStep);
        }

        return list;
    }

    @Override
    protected QueryResponse buildResponse(QueryContext context) {
        User user = context.get("user");
        Credit credit = context.get("credit"); // May be null if not executed
        return new QueryResponse(user, credit);
    }
}
```

#### **Write Service Development Steps (Using `CreateUserService` as Example)**

1.  Define `CreateUserCommand`
2.  Inherit `CommandTemplate<Cmd, Resp>`
3.  Inject required `CommandStep`
4.  Implement `steps(command, context)`, `buildResponse` (can skip `validate` if not needed)
5.  Add `@CommandFlow`, `@Service`, `@Transactional`

```java
@CommandFlow(code = "CREATE_USER", desc = "Create user service")
@Service
@Transactional
@RequiredArgsConstructor
public class CreateUserService extends CommandTemplate<CreateUserCommand, CreateResponse> {
    private final ValidateUserCreditStep validateStep;
    private final SaveUserToDbStep saveStep;
    private final SendWelcomeEmailStep sendEmailStep;
    private final SendWelcomeSmsStep sendSmsStep;

    // validate method can be skipped, uses default implementation

    @Override
    protected List<CommandStep<?>> steps(CreateUserCommand command, CommandContext context) {
        List<CommandStep<?>> list = new ArrayList<>();
        list.add(validateStep);
        list.add(saveStep);

        // ✅ Direct use of if-else in steps method, clear and intuitive!
        if ("EMAIL".equals(command.getNotifyType())) {
            list.add(sendEmailStep);
        } else if ("SMS".equals(command.getNotifyType())) {
            list.add(sendSmsStep);
        }

        return list;
    }

    @Override
    protected CreateResponse buildResponse(CommandContext context) {
        User savedUser = context.get("savedUser");
        return new CreateResponse(savedUser.getId());
    }
}
```

#### **Step Development Guide**

Steps are stateless, functional components that only depend on context.

```java
@Component // or use @StepComponent (if you choose to implement it)
public class FetchUserFromDbStep implements QueryStep<User> {
    @Autowired
    private UserRepository userRepo;

    @Override
    public StepResult<User> execute(QueryContext context) {
        QueryRequest request = context.get("request");
        try {
            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            context.put("user", user); // Put result in context for subsequent steps or buildResponse
            return StepResult.success(user);
        } catch (Exception e) {
            return StepResult.failure("Failed to fetch user", "USER_FETCH_FAIL", ErrorType.SYSTEM);
        }
    }
}
```

```java
@Component
public class SaveUserToDbStep implements CommandStep<User> {
    @Autowired
    private UserRepository userRepo;

    @Override
    public StepResult<User> execute(CommandContext context) {
        CreateUserCommand command = context.get("command");
        try {
            User user = new User();
            user.setName(command.getName());
            user.setEmail(command.getEmail());
            User savedUser = userRepo.save(user);
            context.put("savedUser", savedUser); // Put in context
            return StepResult.success(savedUser);
        } catch (Exception e) {
            return StepResult.failure("Failed to save user", "USER_SAVE_FAIL", ErrorType.SYSTEM);
        }
    }
}
```

---

### **🔎 Text Flow Diagram (Developer Quick Visualization)**

#### **Query Flow (Read-only)**

```
Client
  |
  v
Controller (HTTP -> DTO)
  |
  v
QueryService (inherits QueryTemplate)
  |
  |-- validate(request) -> StepResult (optional, has default implementation)
  |
  |-- QueryContext ctx = { request }
  |
  |-- steps(request, ctx) -> returns step list (can include if-else conditions)
  |
  |-- for step in steps():
  |      result = step.execute(ctx)
  |      if !result.success -> throw BusinessException
  |
  |-- response = buildResponse(ctx)
  |
  v
Controller (DTO -> HTTP 200)
  |
  v
Client
```

#### **Command Flow (With Side Effects)**

```
Client
  |
  v
Controller (HTTP -> Command)
  |
  v
CommandService (inherits CommandTemplate)
  |
  |-- validate(command) -> StepResult (optional, has default implementation)
  |
  |-- CommandContext ctx = { command }
  |
  |-- steps(command, ctx) -> returns step list (can include if-else conditions)
  |
  |-- for step in steps():
  |      result = step.execute(ctx)
  |      if !result.success -> throw BusinessException
  |      (steps can write DB, call external APIs, publish events)
  |
  |-- response = buildResponse(ctx)
  |
  v
Controller (DTO -> HTTP 201/200)
  |
  v
Client
```

---

### **✅ Controller Layer (Simple and Consistent)**

```java
@RestController
@RequiredArgsConstructor
public class QueryController {
    private final CreditQueryService enquiryService;

    @PostMapping("/api/credit-enquiry")
    public ResponseEntity<QueryResponse> enquiry(@RequestBody QueryRequest req) {
        return ResponseEntity.ok(enquiryService.execute(req));
    }
}

@RestController
@RequiredArgsConstructor
public class CreateUserController {
    private final CreateUserService createUserService;

    @PostMapping("/api/user")
    public ResponseEntity<CreateResponse> create(@RequestBody CreateUserCommand cmd) {
        return ResponseEntity.ok(createUserService.execute(cmd));
    }
}
```

✅ **Advantages**: Controllers are extremely simple, only handling protocol conversion, business logic completely delegated.

---

### **✅ Global Exception Handling (Shared)**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException e) {
        ErrorResponse body = new ErrorResponse(e.getErrorCode(), e.getMessage());
        HttpStatus status = switch (e.getErrorType()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS -> HttpStatus.CONFLICT;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(body);
    }
}
```

---

### **✅ ArchUnit Architecture Auditing (Enforce Compliance)**

```java
@Test
void all_enquiry_services_must_be_annotated() {
    classes().that().areAssignableTo(QueryTemplate.class)
             .should().beAnnotatedWith(QueryFlow.class)
             .check(imported);
}

@Test
void all_command_services_must_be_transactional() {
    classes().that().areAnnotatedWith(CommandFlow.class)
             .should().beAnnotatedWith(Transactional.class)
             .check(imported);
}

// Enforce prohibition of Command services calling each other
@Test
void command_services_should_not_call_other_command_services() {
    ArchRule rule = noClasses()
            .that().areAnnotatedWith(CommandFlow.class)
            .should().callClassesThat().areAnnotatedWith(CommandFlow.class)
            .because("Mutual calls between Command services lead to transactional boundary confusion and must be prohibited. " +
                    "Please extract reusable logic as CommandStep.");

    rule.check(imported);
}
```

✅ **Value**: Automated guarantee of architectural consistency, preventing team members from "free styling" that leads to design erosion.

---

### **📊 Design Comparison Summary**

| Feature | Query (R) | Command (CUD) |
| :--- | :--- | :--- |
| Template Class | `QueryTemplate<R, S>` | `CommandTemplate<C, R>` |
| Context | `QueryContext` | `CommandContext` |
| Step Interface | `QueryStep<T>` | `CommandStep<T>` |
| Service Annotation | `@QueryFlow` | `@CommandFlow` |
| Transaction Control | ❌ No transaction needed | ✅ Must have `@Transactional` |
| Audit Information | Not enforced | ✅ Can be passed through `CommandContext` |
| Typical Steps | Query DB, call API | Query, write DB, publish events, permission check |
| Validation Method | ✅ Has default empty implementation | ✅ Has default empty implementation |
| Conditional Orchestration | ✅ Use `if-else` in `steps(request, ctx)` | ✅ Use `if-else` in `steps(command, ctx)` |

---

### **🏁 Conclusion**

This architecture successfully implements the design philosophy of "making the right things the easiest to do":

*   Through **template method pattern**, enforce process uniformity and eliminate omissions.
*   Through **functional steps + context**, achieve high cohesion and low coupling.
*   Through **query-command separation**, clarify responsibilities and improve system clarity.
*   Through **custom annotations + ArchUnit**, achieve auditable and governable architecture.
*   Through **returning pure business objects**, ensure services can be directly reused by other modules, unbound from HTTP layer.
*   Through **`validate` default implementation**, simplify development for simple scenarios.
*   Through **`steps(request, context)` + `if-else`**, make conditional process orchestration intuitive and understandable.
*   Through **common `ErrorType` enum**, ensure consistency and maintainability of error classification.

This framework not only solves current problems but also lays a solid foundation for future **process orchestration, monitoring and alerting, automated testing, and visual operations**.

✅ **Recommended for comprehensive adoption as team standard development specification.**

---

This document is now the truly complete version, containing all details from core components to specific usage. You can use it as the official specification document for the project.
