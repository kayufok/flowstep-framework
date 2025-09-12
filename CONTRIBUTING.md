# 🤝 Contributing to FlowStep Framework

Thank you for your interest in contributing to the FlowStep Framework! This guide will help you get started.

## 🎯 How to Contribute

### 1. **Report Issues**
- Use our [issue templates](https://github.com/kayufok/flowstep-framework/issues/new/choose)
- Provide clear, reproducible steps
- Include relevant logs and environment details

### 2. **Suggest Features**
- Open a [feature request](https://github.com/kayufok/flowstep-framework/issues/new?template=feature_request.md)
- Explain the use case and expected behavior
- Discuss design considerations

### 3. **Submit Code**
- Fork the repository
- Create a feature branch
- Write tests for new functionality
- Ensure all tests pass
- Submit a Pull Request

## 🛠️ Development Setup

### Prerequisites
- Java 21 or higher
- Git
- IDE with Lombok support (IntelliJ IDEA, VS Code, etc.)

### Local Development
```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/flowstep-framework.git
cd flowstep-framework

# Build and test
./gradlew build

# Run tests
./gradlew test

# Run architecture tests
./gradlew test --tests "*ArchitectureTest*"
```

## 📋 Pull Request Guidelines

### Before Submitting
- [ ] Code builds without warnings
- [ ] All tests pass
- [ ] New functionality has comprehensive tests
- [ ] JavaDoc is complete for public APIs
- [ ] Code follows existing patterns and conventions
- [ ] Architecture tests pass

### PR Description Template
```markdown
## Summary
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code builds clean
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Breaking changes documented
```

## 🧪 Testing Standards

### Unit Tests
- Test all public methods
- Use descriptive test names (`should[ExpectedBehavior]When[StateUnderTest]`)
- Follow AAA pattern (Arrange, Act, Assert)
- Mock external dependencies

```java
@Test
void shouldReturnUserWhenValidIdProvided() {
    // Arrange
    QueryContext context = new QueryContext();
    context.setRequest(new UserQuery(123L));
    
    // Act
    StepResult<User> result = fetchUserStep.execute(context);
    
    // Assert
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isNotNull();
}
```

### Integration Tests
- Test complete workflows
- Use `@SpringBootTest` for Spring context
- Test auto-configuration behavior

### Architecture Tests
- Verify design principles with ArchUnit
- Ensure layer separation
- Prevent architectural drift

## 📝 Code Style Guidelines

### General Principles
- **Clean Code**: Write self-documenting, readable code
- **SOLID Principles**: Follow dependency inversion, single responsibility, etc.
- **Template Method**: Maintain consistent execution flow
- **CQRS Separation**: Keep queries and commands separate

### Naming Conventions
- **Classes**: PascalCase (e.g., `UserQueryService`)
- **Methods**: camelCase (e.g., `buildResponse`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- **Packages**: lowercase (e.g., `net.xrftech.flowstep`)

### JavaDoc Standards
```java
/**
 * Executes a query step within the provided context.
 * 
 * This method should perform read-only operations and communicate
 * results through the context object.
 * 
 * @param context the query context containing request data and shared state
 * @return the result of this step's execution
 * @throws IllegalArgumentException if context is null
 * @since 1.0.0
 */
StepResult<T> execute(QueryContext context);
```

## 🏗️ Architecture Guidelines

### Package Organization
```
net.xrftech.flowstep/
├── annotation/     # Framework annotations
├── context/        # Context classes
├── exception/      # Error handling
├── step/          # Step interfaces and utilities
├── config/        # Auto-configuration
├── QueryTemplate   # Query template
└── CommandTemplate # Command template
```

### Design Patterns
- **Template Method**: Core execution flow
- **Strategy Pattern**: Conditional step execution
- **Builder Pattern**: Complex object creation
- **Factory Pattern**: Step creation (where applicable)

## 🚀 Release Process

### Version Strategy
We follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Release Checklist
- [ ] All tests pass on CI
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version bumped appropriately
- [ ] Release notes prepared

## 🐛 Bug Reports

### Security Issues
**DO NOT** open public issues for security vulnerabilities. Instead:
- Email: [security@xrftech.net](mailto:security@xrftech.net)
- Use GitHub's private security reporting

### Bug Report Template
```markdown
**Bug Description**
Clear description of the issue

**Steps to Reproduce**
1. Step one
2. Step two
3. Step three

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Environment**
- FlowStep Version: x.x.x
- Java Version: xx
- Spring Boot Version: x.x.x
- OS: Operating System
```

## 🎯 Feature Requests

### Before Requesting
- Check existing issues and discussions
- Consider if it fits the framework's scope
- Think about backward compatibility

### Feature Request Template
```markdown
**Problem Statement**
What problem does this solve?

**Proposed Solution**
How should it work?

**Alternative Solutions**
Other approaches considered?

**Implementation Notes**
Technical considerations
```

## 🌟 Recognition

### Contributors
All contributors are recognized in:
- README.md contributors section
- CHANGELOG.md for each release
- GitHub contributors page

### Contribution Types
- 💻 Code contributions
- 📖 Documentation improvements
- 🐛 Bug reports and fixes
- 💡 Feature suggestions
- 🧪 Test improvements
- 🎨 Design and UX improvements

## 📞 Getting Help

### Community Channels
- **GitHub Discussions**: General questions and discussions
- **GitHub Issues**: Bug reports and feature requests
- **Stack Overflow**: Tag questions with `flowstep-framework`

### Maintainer Response Times
- **Critical bugs**: 24-48 hours
- **Feature requests**: 3-7 days
- **General questions**: 7-14 days

## 📄 Legal

### Code of Conduct
This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to uphold this code.

### License Agreement
By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for helping make FlowStep Framework better! 🚀**

*For questions about contributing, reach out via [GitHub Discussions](https://github.com/kayufok/flowstep-framework/discussions)*
