# 📚 FlowStep Documentation

Welcome to the FlowStep Spring Boot Starter documentation! This comprehensive guide covers everything you need to know about using, configuring, and extending FlowStep in your Spring Boot applications.

## 🎯 What is FlowStep?

FlowStep is a Spring Boot starter that implements the **Template Method** and **CQRS** (Command Query Responsibility Segregation) patterns to provide a consistent, maintainable architecture for Spring Boot applications. It enforces clean separation between read and write operations while providing a step-based execution model for complex business logic.

## 📖 Documentation Index

### Core Documentation

| Document | Description |
|----------|-------------|
| [API Reference](./API_REFERENCE.md) | Complete API documentation for all FlowStep classes, interfaces, and annotations |
| [Architecture Guide](./ARCHITECTURE.md) | In-depth architecture documentation including design patterns, component structure, and execution flows |
| [Usage Guide](./USAGE_GUIDE.md) | Practical guide with examples for implementing queries, commands, and common patterns |
| [Testing Guide](./TESTING_GUIDE.md) | Comprehensive testing strategies, utilities, and best practices for FlowStep applications |
| [Configuration Reference](./CONFIGURATION_REFERENCE.md) | Complete configuration options, properties, and customization guide |

### Quick Links

- [Getting Started](#getting-started)
- [Key Features](#key-features)
- [Version Compatibility](#version-compatibility)
- [Support](#support)

## 🚀 Getting Started

### 1. Choose Your Version

FlowStep provides two starter modules:

| Starter | Java Version | Spring Boot Version |
|---------|--------------|---------------------|
| `flowstep-spring-boot-2-starter` | Java 8+ | Spring Boot 2.7.x+ |
| `flowstep-spring-boot-3-starter` | Java 17+ | Spring Boot 3.x+ |

### 2. Add Dependency

#### Maven (Spring Boot 2.x)
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Maven (Spring Boot 3.x)
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. Create Your First Service

#### Query (Read) Operation
```java
@QueryFlow(code = "GET_USER", desc = "Retrieve user details")
@Service
public class GetUserQuery extends QueryTemplate<Long, UserDto> {
    
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
    protected UserDto buildResponse(QueryContext context) {
        User user = context.get("user");
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
```

#### Command (Write) Operation
```java
@CommandFlow(code = "CREATE_USER", desc = "Create new user")
@Service
@Transactional
public class CreateUserCommand extends CommandTemplate<CreateUserRequest, CreateUserResponse> {
    
    @Override
    protected List<CommandStep<?>> steps(CreateUserRequest request, CommandContext context) {
        return List.of(
            () -> {
                User user = new User(request.getName(), request.getEmail());
                User saved = userRepository.save(user);
                context.put("user", saved);
                return StepResult.success(saved);
            }
        );
    }
    
    @Override
    protected CreateUserResponse buildResponse(CommandContext context) {
        User user = context.get("user");
        return new CreateUserResponse(user.getId(), user.getEmail());
    }
}
```

## ✨ Key Features

### 🏗️ Clean Architecture
- **CQRS Pattern**: Clear separation between read and write operations
- **Template Method**: Consistent execution flow across all services
- **Step-Based Design**: Modular, testable business logic components

### 🛡️ Robust Error Handling
- **Type-Safe Errors**: Comprehensive error classification (Validation, Business, System)
- **Global Exception Handler**: Automatic REST API error responses
- **Contextual Error Information**: Rich error details for debugging

### ⚡ Spring Boot Integration
- **Zero Configuration**: Works out of the box with sensible defaults
- **Auto-Configuration**: Seamless Spring Boot integration
- **Conditional Features**: Smart feature activation based on classpath

### 🧪 Testing Support
- **Step Isolation**: Test individual business logic steps
- **Context Mocking**: Easy test data management
- **Architecture Testing**: Optional ArchUnit integration

### 📊 Enterprise Features
- **Transaction Management**: Automatic transaction handling for commands
- **Event Publishing**: Built-in event support for event-driven architectures
- **Audit Trail**: Automatic audit information capture
- **Multi-Version Support**: Compatible with both Spring Boot 2.x and 3.x

## 📋 Version Compatibility

### Spring Boot 2.x Starter
- **Java**: 8, 11
- **Spring Boot**: 2.7.x+
- **Spring Framework**: 5.3.x+
- **Validation**: javax.validation

### Spring Boot 3.x Starter
- **Java**: 17, 21
- **Spring Boot**: 3.0.x+
- **Spring Framework**: 6.0.x+
- **Validation**: jakarta.validation

## 🎯 Use Cases

FlowStep is ideal for:
- ✅ Enterprise applications requiring consistent architecture
- ✅ Microservices with complex business logic
- ✅ Applications needing clear audit trails
- ✅ Systems requiring event-driven capabilities
- ✅ Projects prioritizing testability and maintainability

## 📊 Documentation Coverage

Our documentation covers:

| Topic | Coverage | Document |
|-------|----------|----------|
| API Documentation | Complete reference for all public APIs | [API Reference](./API_REFERENCE.md) |
| Architecture | Design patterns, components, flows | [Architecture Guide](./ARCHITECTURE.md) |
| Implementation | Step-by-step guides with examples | [Usage Guide](./USAGE_GUIDE.md) |
| Testing | Unit, integration, and architecture tests | [Testing Guide](./TESTING_GUIDE.md) |
| Configuration | All properties and customization options | [Configuration Reference](./CONFIGURATION_REFERENCE.md) |

## 🔍 Finding Information

### By Task

| I want to... | See Document |
|--------------|--------------|
| Understand the architecture | [Architecture Guide](./ARCHITECTURE.md) |
| Implement a query service | [Usage Guide - Basic Query](./USAGE_GUIDE.md#basic-query-implementation) |
| Implement a command service | [Usage Guide - Basic Command](./USAGE_GUIDE.md#basic-command-implementation) |
| Write tests for my services | [Testing Guide](./TESTING_GUIDE.md) |
| Configure FlowStep properties | [Configuration Reference](./CONFIGURATION_REFERENCE.md) |
| Understand error handling | [API Reference - Exception Classes](./API_REFERENCE.md#exception-classes) |
| Use advanced patterns | [Usage Guide - Advanced Patterns](./USAGE_GUIDE.md#advanced-patterns) |

### By Component

| Component | Documentation |
|-----------|---------------|
| QueryTemplate | [API Reference](./API_REFERENCE.md#querytemplate) |
| CommandTemplate | [API Reference](./API_REFERENCE.md#commandtemplate) |
| Context Classes | [API Reference](./API_REFERENCE.md#context-classes) |
| Step Interfaces | [API Reference](./API_REFERENCE.md#step-interfaces) |
| Annotations | [API Reference](./API_REFERENCE.md#annotations) |
| Configuration | [Configuration Reference](./CONFIGURATION_REFERENCE.md) |

## 💡 Best Practices Summary

### Design Principles
1. **Single Responsibility**: Each step should have one clear purpose
2. **Explicit Over Implicit**: Clear method names and error messages
3. **Fail Fast**: Validate early and provide clear error messages
4. **Immutable Data**: Prefer immutable objects in context

### Implementation Guidelines
1. **Keep Steps Small**: 3-7 steps per service is ideal
2. **Use Meaningful Keys**: Clear, descriptive context keys
3. **Handle Errors Gracefully**: Use appropriate error types
4. **Test Thoroughly**: Unit test steps, integration test flows

### Performance Tips
1. **Optimize Database Access**: Batch operations when possible
2. **Use Caching**: Cache query results when appropriate
3. **Async When Possible**: Use async for non-critical operations
4. **Monitor Performance**: Add metrics to critical paths

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](../CONTRIBUTING.md) for details on:
- Code style and standards
- Testing requirements
- Pull request process
- Development setup

## 📞 Support

### Getting Help
- **Documentation**: Start with this comprehensive documentation
- **Issues**: [GitHub Issues](https://github.com/kayufok/flowstep-spring-boot-starter/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kayufok/flowstep-spring-boot-starter/discussions)

### Reporting Issues
When reporting issues, please include:
- FlowStep version
- Spring Boot version
- Java version
- Minimal reproducible example
- Stack traces (if applicable)

## 📄 License

FlowStep is licensed under the MIT License. See [LICENSE](../LICENSE) for details.

## 🌟 Acknowledgments

FlowStep is inspired by:
- Domain-Driven Design principles
- CQRS pattern implementations
- Template Method pattern
- Spring Boot best practices

## 📈 Roadmap

Planned features:
- Async command support
- Saga pattern implementation
- GraphQL integration
- Metrics and tracing
- Additional step types

---

**Thank you for choosing FlowStep!** We hope this documentation helps you build robust, maintainable Spring Boot applications.

For the latest updates and examples, visit our [GitHub repository](https://github.com/kayufok/flowstep-spring-boot-starter).