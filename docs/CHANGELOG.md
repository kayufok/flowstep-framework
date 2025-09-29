# FlowStep Framework Changelog

All notable changes to the FlowStep Framework will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-09-29

### Added
- **QueryStep Support in CommandFlow**: CommandTemplate now supports mixing QueryStep and CommandStep instances in the same command flow
  - Enables reuse of existing QueryStep components within command operations
  - Provides better separation between read and write operations in command flows
  - Maintains backward compatibility with existing code

### Changed
- **CommandTemplate.steps() Method Signature**: Changed return type from `List<CommandStep<?>>` to `List<?>` to support both CommandStep and QueryStep
  - This is a source-compatible change for existing implementations
  - Existing code that only uses CommandStep will continue to work without modification
  - The framework now handles both step types transparently

### Technical Details

#### QueryStep in CommandFlow Implementation
The CommandTemplate now includes an adapter pattern that allows QueryStep instances to execute within a CommandContext. When a QueryStep is encountered:
1. A QueryContext adapter is created that delegates to the underlying CommandContext
2. The QueryStep executes using this adapted context
3. All data stored by the QueryStep is available to subsequent steps through the shared context

#### Usage Example
```java
@CommandFlow(code = "MIXED_FLOW", desc = "Command with mixed step types")
@Service
@Transactional
public class MixedCommand extends CommandTemplate<Request, Response> {
    
    @Autowired
    private FetchDataStep fetchStep; // QueryStep
    
    @Autowired
    private SaveDataStep saveStep; // CommandStep
    
    @Override
    protected List<?> steps(Request request, CommandContext context) {
        return List.of(
            fetchStep,     // QueryStep - read operation
            saveStep,      // CommandStep - write operation
            
            // Inline QueryStep
            (QueryStep<?>) (ctx) -> {
                // Read-only operation
                return StepResult.success();
            },
            
            // Inline CommandStep
            (CommandStep<?>) (ctx) -> {
                // Write operation
                return StepResult.success();
            }
        );
    }
}
```

### Migration Guide

#### For Existing CommandTemplate Implementations
If you have existing CommandTemplate implementations, you have two options:

**Option 1: Keep existing code (recommended for simple cases)**
```java
// This continues to work without changes
@Override
protected List<CommandStep<?>> steps(Command cmd, CommandContext ctx) {
    return List.of(step1, step2, step3);
}
```

**Option 2: Update to use mixed steps (recommended for complex flows)**
```java
// Update return type to List<?>
@Override
protected List<?> steps(Command cmd, CommandContext ctx) {
    return List.of(
        queryStep,    // Can now include QueryStep
        commandStep,  // Along with CommandStep
        queryStep2    // Mix as needed
    );
}
```

### Benefits
1. **Code Reusability**: Existing QueryStep components can be reused in command flows
2. **Clear Separation**: Read operations (QueryStep) and write operations (CommandStep) are clearly distinguished
3. **Flexibility**: Commands can compose both read and write operations as needed
4. **Backward Compatibility**: Existing code continues to work without modification

### Notes
- Both Spring Boot 2.x and 3.x starters have been updated with this feature
- The change maintains full backward compatibility
- No breaking changes to existing APIs

## [1.0.0] - Previous Release

### Initial Features
- Template Method pattern implementation for queries and commands
- CQRS pattern support with QueryTemplate and CommandTemplate
- Step-based execution model
- Context-based communication between steps
- Built-in error handling with ErrorType classification
- Spring Boot auto-configuration
- Support for both Spring Boot 2.7.x and 3.x

---

For more information about the FlowStep Framework, see the [main documentation](README.md).