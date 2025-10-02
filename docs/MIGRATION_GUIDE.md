# 🔄 FlowStep Migration Guide

## Overview

This guide helps you migrate existing Spring Boot applications to use the FlowStep Framework, or upgrade between FlowStep versions.

## Migrating from Plain Spring Services

### Step 1: Add FlowStep Dependency

**For Spring Boot 2.7.x (Java 8+):**
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**For Spring Boot 3.x (Java 17+):**
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Step 2: Identify Service Types

Classify your existing services:
- **Read operations** (queries, fetches) → Migrate to `QueryTemplate`
- **Write operations** (create, update, delete) → Migrate to `CommandTemplate`

### Step 3: Refactor Read Services

**Before (Traditional Service):**
```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserDTO getUserProfile(Long userId) {
        // Validation
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        // Fetch user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Transform to DTO
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}
```

**After (FlowStep QueryTemplate):**
```java
@QueryFlow(code = "GET_USER_PROFILE", desc = "Retrieve user profile")
@Service
public class GetUserProfileQuery extends QueryTemplate<Long, UserDTO> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected StepResult<Void> validate(Long userId) {
        if (userId == null || userId <= 0) {
            return StepResult.failure("Invalid user ID", "VAL_001", ErrorType.VALIDATION);
        }
        return StepResult.success();
    }
    
    @Override
    protected List<QueryStep<?>> steps(Long userId, QueryContext context) {
        return List.of(
            () -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                context.put("user", user);
                return StepResult.success(user);
            }
        );
    }
    
    @Override
    protected UserDTO buildResponse(QueryContext context) {
        User user = context.get("user");
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}
```

### Step 4: Refactor Write Services

**Before (Traditional Service):**
```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public UserDTO createUser(CreateUserRequest request) {
        // Validation
        if (request.getEmail() == null) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        User saved = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(saved.getEmail());
        
        return new UserDTO(saved.getId(), saved.getName(), saved.getEmail());
    }
}
```

**After (FlowStep CommandTemplate):**
```java
@CommandFlow(code = "CREATE_USER", desc = "Create new user account")
@Service
@Transactional
public class CreateUserCommand extends CommandTemplate<CreateUserRequest, UserDTO> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    protected StepResult<Void> validate(CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return StepResult.failure("Email is required", "VAL_002", ErrorType.VALIDATION);
        }
        return StepResult.success();
    }
    
    @Override
    protected List<CommandStep<?>> steps(CreateUserRequest request, CommandContext context) {
        return List.of(
            // Step 1: Create and save user
            () -> {
                User user = new User();
                user.setName(request.getName());
                user.setEmail(request.getEmail());
                User saved = userRepository.save(user);
                context.put("user", saved);
                return StepResult.success(saved);
            },
            // Step 2: Send welcome email
            () -> {
                User user = context.get("user");
                emailService.sendWelcomeEmail(user.getEmail());
                return StepResult.success();
            }
        );
    }
    
    @Override
    protected UserDTO buildResponse(CommandContext context) {
        User user = context.get("user");
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}
```

## Upgrading Between FlowStep Versions

### From Spring Boot 2 to Spring Boot 3

When upgrading your application from Spring Boot 2 to Spring Boot 3:

**Step 1: Update Dependency**
```xml
<!-- Remove -->
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Add -->
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Step 2: Update Imports**

Change `javax` imports to `jakarta`:
```java
// Before
import javax.validation.constraints.NotNull;

// After
import jakarta.validation.constraints.NotNull;
```

**Step 3: Update Java Version**

Ensure your project uses Java 17 or later:
```xml
<properties>
    <java.version>17</java.version>
</properties>
```

## Common Migration Challenges

### Challenge 1: Shared Logic Between Services

**Problem:** Multiple services share common logic.

**Solution:** Extract shared logic into reusable steps:
```java
@Component
public class ValidateUserStep implements QueryStep<User> {
    @Override
    public StepResult<User> execute(QueryContext context) {
        // Common validation logic
        return StepResult.success(user);
    }
}

// Use in multiple services
@Override
protected List<QueryStep<?>> steps(UserRequest request, QueryContext context) {
    return List.of(
        validateUserStep,  // Reusable step
        // other steps...
    );
}
```

### Challenge 2: Complex Business Logic

**Problem:** Service has complex branching logic.

**Solution:** Use conditional step execution:
```java
@Override
protected List<CommandStep<?>> steps(OrderCommand command, CommandContext context) {
    List<CommandStep<?>> steps = new ArrayList<>();
    steps.add(validateOrderStep);
    
    if (command.requiresPayment()) {
        steps.add(processPaymentStep);
    }
    
    if (command.hasShipping()) {
        steps.add(calculateShippingStep);
    }
    
    steps.add(finalizeOrderStep);
    return steps;
}
```

### Challenge 3: Error Handling

**Problem:** Need to maintain existing error handling behavior.

**Solution:** Use appropriate ErrorType:
```java
return StepResult.failure(
    "User not found",
    "USER_NOT_FOUND",
    ErrorType.BUSINESS  // Maps to 409 Conflict
);
```

## Best Practices for Migration

1. **Migrate Incrementally**: Start with simple services, then move to complex ones
2. **Test Thoroughly**: Ensure all existing tests pass after migration
3. **Extract Steps**: Break down complex services into reusable steps
4. **Use Type-Safe Context**: Store strongly-typed objects in context
5. **Add Annotations**: Use `@QueryFlow` and `@CommandFlow` for documentation
6. **Maintain Transactions**: Keep `@Transactional` on command services

## Verification Checklist

After migration, verify:
- [ ] All tests pass
- [ ] Error responses match original behavior
- [ ] Transaction boundaries are correct
- [ ] Performance is comparable or better
- [ ] Logging and monitoring still work
- [ ] API contracts remain unchanged

## Getting Help

If you encounter issues during migration:
- Check the [Usage Guide](./USAGE_GUIDE.md) for examples
- Review [Architecture Guide](./ARCHITECTURE.md) for design patterns
- See [Testing Guide](./TESTING_GUIDE.md) for testing strategies
- Ask questions in [GitHub Discussions](https://github.com/kayufok/flowstep-framework/discussions)

---

**Need help with migration?** Feel free to open an issue or discussion in our GitHub repository!
