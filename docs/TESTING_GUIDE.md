# 🧪 FlowStep Testing Guide

## Table of Contents
1. [Testing Philosophy](#testing-philosophy)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [Architecture Testing](#architecture-testing)
5. [Test Utilities](#test-utilities)
6. [Best Practices](#best-practices)
7. [Common Testing Scenarios](#common-testing-scenarios)

## Testing Philosophy

FlowStep's architecture naturally promotes testability through:
- **Step Isolation**: Each step can be tested independently
- **Context Injection**: Easy to mock and verify context interactions
- **Clear Boundaries**: Well-defined inputs and outputs
- **Separation of Concerns**: Business logic separated from infrastructure

### Testing Pyramid

```
         /\
        /  \  E2E Tests (Few)
       /    \
      /------\  Integration Tests (Some)
     /        \
    /----------\  Unit Tests (Many)
   /            \
  /--------------\  
```

## Unit Testing

### Testing Individual Steps

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceStepsTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private InventoryService inventoryService;
    
    private OrderService orderService;
    
    @BeforeEach
    void setUp() {
        orderService = new OrderService(productRepository, inventoryService);
    }
    
    @Test
    @DisplayName("Should validate products successfully when all products exist")
    void validateProducts_Success() {
        // Given
        CommandContext context = new CommandContext();
        OrderCommand command = OrderCommand.builder()
            .items(List.of(
                new OrderItem(1L, 2),
                new OrderItem(2L, 1)
            ))
            .build();
        context.setCommand(command);
        
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsById(2L)).thenReturn(true);
        
        // When
        StepResult<Void> result = orderService.validateProducts(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(productRepository, times(2)).existsById(any());
    }
    
    @Test
    @DisplayName("Should fail validation when product doesn't exist")
    void validateProducts_ProductNotFound() {
        // Given
        CommandContext context = new CommandContext();
        OrderCommand command = OrderCommand.builder()
            .items(List.of(new OrderItem(999L, 1)))
            .build();
        context.setCommand(command);
        
        when(productRepository.existsById(999L)).thenReturn(false);
        
        // When
        StepResult<Void> result = orderService.validateProducts(context);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorCode()).isEqualTo("PROD_NOT_FOUND");
        assertThat(result.getErrorType()).isEqualTo(ErrorType.BUSINESS);
        assertThat(result.getMessage()).contains("Product 999 not found");
    }
}
```

### Testing Validation Logic

```java
class CreateUserCommandValidationTest {
    
    private CreateUserCommand command;
    
    @BeforeEach
    void setUp() {
        command = new CreateUserCommand(null, null, null);
    }
    
    @Test
    void shouldValidateSuccessfully() {
        // Given
        UserRequest request = UserRequest.builder()
            .email("user@example.com")
            .username("johndoe")
            .password("SecurePass123!")
            .build();
        
        // When
        StepResult<Void> result = command.validate(request);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",
        "@example.com",
        "user@",
        "user..name@example.com"
    })
    void shouldFailValidationForInvalidEmail(String email) {
        // Given
        UserRequest request = UserRequest.builder()
            .email(email)
            .username("validusername")
            .password("ValidPass123!")
            .build();
        
        // When
        StepResult<Void> result = command.validate(request);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorType()).isEqualTo(ErrorType.VALIDATION);
        assertThat(result.getMessage()).containsIgnoringCase("email");
    }
    
    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void shouldFailValidationForWeakPassword(String password, String expectedError) {
        // Given
        UserRequest request = UserRequest.builder()
            .email("user@example.com")
            .username("validusername")
            .password(password)
            .build();
        
        // When
        StepResult<Void> result = command.validate(request);
        
        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getMessage()).contains(expectedError);
    }
    
    static Stream<Arguments> invalidPasswordProvider() {
        return Stream.of(
            Arguments.of("short", "at least 8 characters"),
            Arguments.of("alllowercase", "uppercase"),
            Arguments.of("ALLUPPERCASE", "lowercase"),
            Arguments.of("NoNumbers!", "number"),
            Arguments.of("NoSpecialChar1", "special character")
        );
    }
}
```

### Testing Context Interactions

```java
@Test
void shouldStoreAndRetrieveFromContext() {
    // Given
    QueryContext context = new QueryContext();
    User user = User.builder()
        .id(1L)
        .name("John Doe")
        .email("john@example.com")
        .build();
    
    // When
    context.put("user", user);
    context.put("timestamp", LocalDateTime.now());
    context.put("count", 42);
    
    // Then
    assertThat(context.<User>get("user")).isEqualTo(user);
    assertThat(context.has("timestamp")).isTrue();
    assertThat(context.<Integer>get("count")).isEqualTo(42);
    assertThat(context.get("nonexistent")).isNull();
}

@Test
void shouldHandleDefaultValues() {
    // Given
    QueryContext context = new QueryContext();
    
    // When
    String value = context.getOrDefault("missing", "default");
    Integer count = context.getOrDefault("count", 0);
    
    // Then
    assertThat(value).isEqualTo("default");
    assertThat(count).isEqualTo(0);
}
```

### Testing Response Building

```java
@Test
void shouldBuildResponseCorrectly() {
    // Given
    QueryContext context = new QueryContext();
    User user = createTestUser();
    List<Order> orders = createTestOrders();
    UserPreferences preferences = createTestPreferences();
    
    context.put("user", user);
    context.put("orders", orders);
    context.put("preferences", preferences);
    
    UserProfileQuery query = new UserProfileQuery(null, null, null);
    
    // When
    UserProfileResponse response = query.buildResponse(context);
    
    // Then
    assertThat(response).isNotNull();
    assertThat(response.getUserId()).isEqualTo(user.getId());
    assertThat(response.getUsername()).isEqualTo(user.getUsername());
    assertThat(response.getOrderCount()).isEqualTo(orders.size());
    assertThat(response.getPreferences()).isEqualTo(preferences);
}
```

## Integration Testing

### Testing Complete Query Flow

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProfileQueryIntegrationTest {
    
    @Autowired
    private UserProfileQuery userProfileQuery;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void shouldExecuteCompleteQueryFlow() throws BusinessException {
        // Given
        User user = userRepository.save(createTestUser());
        orderRepository.saveAll(createOrdersForUser(user.getId()));
        
        UserProfileRequest request = new UserProfileRequest(user.getId());
        
        // When
        UserProfileResponse response = userProfileQuery.execute(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getOrders()).hasSize(3);
        assertThat(response.getTotalSpent()).isPositive();
    }
    
    @Test
    void shouldHandleUserNotFound() {
        // Given
        UserProfileRequest request = new UserProfileRequest(999L);
        
        // When/Then
        assertThatThrownBy(() -> userProfileQuery.execute(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", "USER_NOT_FOUND")
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BUSINESS);
    }
}
```

### Testing Complete Command Flow

```java
@SpringBootTest
@Transactional
@Rollback
class CreateOrderCommandIntegrationTest {
    
    @Autowired
    private CreateOrderCommand createOrderCommand;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @MockBean
    private PaymentService paymentService;
    
    @MockBean
    private EventPublisher eventPublisher;
    
    @Test
    void shouldCreateOrderSuccessfully() throws BusinessException {
        // Given
        Product product1 = productRepository.save(createProduct("Product 1", 10.00));
        Product product2 = productRepository.save(createProduct("Product 2", 20.00));
        
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .items(List.of(
                new OrderItem(product1.getId(), 2),
                new OrderItem(product2.getId(), 1)
            ))
            .paymentMethod("CREDIT_CARD")
            .build();
        
        when(paymentService.processPayment(any()))
            .thenReturn(PaymentResult.success("PAY-123"));
        
        // When
        CreateOrderResponse response = createOrderCommand.execute(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getTotal()).isEqualTo(new BigDecimal("40.00"));
        
        // Verify order was saved
        Order savedOrder = orderRepository.findById(response.getOrderId()).orElse(null);
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getItems()).hasSize(2);
        
        // Verify event was published
        verify(eventPublisher).publish(argThat(event -> 
            event instanceof OrderCreatedEvent &&
            ((OrderCreatedEvent) event).getOrderId().equals(response.getOrderId())
        ));
    }
    
    @Test
    void shouldRollbackOnPaymentFailure() {
        // Given
        Product product = productRepository.save(createProduct("Product", 10.00));
        
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .items(List.of(new OrderItem(product.getId(), 1)))
            .paymentMethod("CREDIT_CARD")
            .build();
        
        when(paymentService.processPayment(any()))
            .thenReturn(PaymentResult.failure("Insufficient funds"));
        
        long orderCountBefore = orderRepository.count();
        
        // When/Then
        assertThatThrownBy(() -> createOrderCommand.execute(request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", "PAYMENT_FAILED");
        
        // Verify rollback
        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
        verify(eventPublisher, never()).publish(any());
    }
}
```

### Testing REST Endpoints

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldCreateOrderViaRestEndpoint() throws Exception {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .items(List.of(new OrderItem(1L, 2)))
            .paymentMethod("CREDIT_CARD")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.orderNumber").exists())
            .andExpect(header().exists("Location"));
    }
    
    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        // Given
        String invalidRequest = """
            {
                "customerId": null,
                "items": []
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").exists())
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldHandleBusinessExceptionCorrectly() throws Exception {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(999L) // Non-existent customer
            .items(List.of(new OrderItem(1L, 1)))
            .build();
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andReturn();
        
        ErrorResponse errorResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ErrorResponse.class
        );
        
        assertThat(errorResponse.getErrorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
        assertThat(errorResponse.getMessage()).contains("Customer");
    }
}
```

## Architecture Testing

### Using ArchUnit

```java
@AnalyzeClasses(packages = "net.xrftech.flowstep")
class ArchitectureTest {
    
    @ArchTest
    static final ArchRule queryServicesShouldExtendQueryTemplate = 
        classes()
            .that().areAnnotatedWith(QueryFlow.class)
            .should().beAssignableTo(QueryTemplate.class)
            .because("All query services must extend QueryTemplate");
    
    @ArchTest
    static final ArchRule commandServicesShouldExtendCommandTemplate = 
        classes()
            .that().areAnnotatedWith(CommandFlow.class)
            .should().beAssignableTo(CommandTemplate.class)
            .because("All command services must extend CommandTemplate");
    
    @ArchTest
    static final ArchRule commandServicesShouldBeTransactional = 
        classes()
            .that().areAnnotatedWith(CommandFlow.class)
            .should().beAnnotatedWith(Transactional.class)
            .because("Command services must be transactional");
    
    @ArchTest
    static final ArchRule stepsShouldReturnStepResult = 
        methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..step..")
            .and().arePublic()
            .and().haveName("execute")
            .should().haveRawReturnType(StepResult.class)
            .because("Step execute methods must return StepResult");
    
    @ArchTest
    static final ArchRule contextsShouldExtendBaseContext = 
        classes()
            .that().resideInAPackage("..context..")
            .and().haveSimpleNameEndingWith("Context")
            .should().beAssignableTo(BaseContext.class)
            .because("All contexts must extend BaseContext");
    
    @ArchTest
    static final ArchRule noDirectRepositoryAccessInControllers = 
        noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("Controllers should not directly access repositories");
    
    @ArchTest
    static final ArchRule serviceLayerShouldNotDependOnControllers = 
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .because("Service layer should not depend on controllers");
}
```

### Custom Architecture Rules

```java
@Test
void commandsShouldHaveCorrespondingEvents() {
    JavaClasses classes = new ClassFileImporter().importPackages("com.example");
    
    ArchRule rule = classes().that()
        .areAnnotatedWith(CommandFlow.class)
        .should(new ArchCondition<JavaClass>("publish corresponding events") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String commandName = javaClass.getSimpleName();
                String expectedEventName = commandName.replace("Command", "Event");
                
                boolean hasEvent = javaClass.getCodeUnits().stream()
                    .anyMatch(codeUnit -> 
                        codeUnit.getFullName().contains(expectedEventName)
                    );
                
                if (!hasEvent) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("Command %s should publish %s", 
                            commandName, expectedEventName)
                    ));
                }
            }
        });
    
    rule.check(classes);
}
```

## Test Utilities

### Test Data Builders

```java
public class TestDataBuilder {
    
    public static UserBuilder user() {
        return new UserBuilder();
    }
    
    public static OrderBuilder order() {
        return new OrderBuilder();
    }
    
    public static class UserBuilder {
        private Long id = 1L;
        private String username = "testuser";
        private String email = "test@example.com";
        private UserStatus status = UserStatus.ACTIVE;
        
        public UserBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public UserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder withStatus(UserStatus status) {
            this.status = status;
            return this;
        }
        
        public User build() {
            return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .status(status)
                .build();
        }
    }
}

// Usage
User testUser = TestDataBuilder.user()
    .withUsername("johndoe")
    .withEmail("john@example.com")
    .withStatus(UserStatus.PENDING)
    .build();
```

### Custom Assertions

```java
public class FlowStepAssertions {
    
    public static StepResultAssert assertThatStep(StepResult<?> actual) {
        return new StepResultAssert(actual);
    }
    
    public static class StepResultAssert extends AbstractAssert<StepResultAssert, StepResult<?>> {
        
        public StepResultAssert(StepResult<?> actual) {
            super(actual, StepResultAssert.class);
        }
        
        public StepResultAssert isSuccessful() {
            isNotNull();
            if (!actual.isSuccess()) {
                failWithMessage("Expected successful step result but was failure with message: %s",
                    actual.getMessage());
            }
            return this;
        }
        
        public StepResultAssert isFailure() {
            isNotNull();
            if (actual.isSuccess()) {
                failWithMessage("Expected failed step result but was successful");
            }
            return this;
        }
        
        public StepResultAssert hasErrorCode(String expectedCode) {
            isNotNull();
            isFailure();
            if (!expectedCode.equals(actual.getErrorCode())) {
                failWithMessage("Expected error code %s but was %s",
                    expectedCode, actual.getErrorCode());
            }
            return this;
        }
        
        public StepResultAssert hasErrorType(ErrorType expectedType) {
            isNotNull();
            isFailure();
            if (expectedType != actual.getErrorType()) {
                failWithMessage("Expected error type %s but was %s",
                    expectedType, actual.getErrorType());
            }
            return this;
        }
        
        public StepResultAssert hasMessageContaining(String substring) {
            isNotNull();
            isFailure();
            if (!actual.getMessage().contains(substring)) {
                failWithMessage("Expected message to contain '%s' but was '%s'",
                    substring, actual.getMessage());
            }
            return this;
        }
    }
}

// Usage
assertThatStep(result)
    .isFailure()
    .hasErrorCode("USER_NOT_FOUND")
    .hasErrorType(ErrorType.BUSINESS)
    .hasMessageContaining("User");
```

### Test Context Factory

```java
public class TestContextFactory {
    
    public static QueryContext queryContext() {
        return new QueryContext();
    }
    
    public static QueryContext queryContextWithRequest(Object request) {
        QueryContext context = new QueryContext();
        context.setRequest(request);
        return context;
    }
    
    public static CommandContext commandContext() {
        CommandContext context = new CommandContext();
        context.setTimestamp(LocalDateTime.now());
        return context;
    }
    
    public static CommandContext commandContextWithUser(String userId) {
        CommandContext context = commandContext();
        context.setUserId(userId);
        return context;
    }
    
    public static CommandContext commandContextWithCommand(Object command) {
        CommandContext context = commandContext();
        context.setCommand(command);
        return context;
    }
}
```

## Best Practices

### 1. Test Naming Conventions

```java
// Good test names
@Test
void shouldCreateOrderWhenAllValidationsPass() { }

@Test
void shouldFailWithBusinessExceptionWhenCustomerNotFound() { }

@Test
void shouldReturnEmptyListWhenNoOrdersExist() { }

// Bad test names
@Test
void test1() { }

@Test
void createOrder() { }

@Test
void error() { }
```

### 2. Test Organization

```java
@Nested
@DisplayName("CreateOrderCommand Tests")
class CreateOrderCommandTest {
    
    @Nested
    @DisplayName("Validation")
    class ValidationTests {
        @Test
        void shouldPassValidationWithValidRequest() { }
        
        @Test
        void shouldFailValidationWithEmptyItems() { }
    }
    
    @Nested
    @DisplayName("Inventory Check")
    class InventoryCheckTests {
        @Test
        void shouldPassWhenInventoryAvailable() { }
        
        @Test
        void shouldFailWhenInventoryInsufficient() { }
    }
    
    @Nested
    @DisplayName("Payment Processing")
    class PaymentTests {
        @Test
        void shouldProcessPaymentSuccessfully() { }
        
        @Test
        void shouldHandlePaymentDecline() { }
    }
}
```

### 3. Test Data Management

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceTest {
    
    @BeforeAll
    void setupTestData() {
        // Setup shared test data once
    }
    
    @BeforeEach
    void setupTest() {
        // Setup test-specific data
    }
    
    @AfterEach
    void cleanupTest() {
        // Clean up test-specific data
    }
    
    @AfterAll
    void cleanupTestData() {
        // Clean up shared test data
    }
}
```

### 4. Mocking Best Practices

```java
// Good: Specific mocking
when(userRepository.findById(1L))
    .thenReturn(Optional.of(testUser));

// Bad: Over-mocking
when(userRepository.findById(any()))
    .thenReturn(Optional.of(testUser));

// Good: Verify specific interactions
verify(emailService).sendEmail(
    argThat(email -> 
        email.getTo().equals("user@example.com") &&
        email.getSubject().contains("Welcome")
    )
);

// Bad: No verification
// Missing verify statements
```

### 5. Test Coverage Guidelines

```java
// Aim for high coverage in:
// - Validation logic (100%)
// - Business rules (90%+)
// - Error handling (90%+)
// - Response building (90%+)

// Lower coverage acceptable for:
// - Simple getters/setters
// - Framework integration code
// - Auto-generated code
```

## Common Testing Scenarios

### Testing Async Operations

```java
@Test
void shouldHandleAsyncOperations() {
    // Given
    CommandContext context = new CommandContext();
    CompletableFuture<String> asyncResult = CompletableFuture
        .supplyAsync(() -> {
            // Simulate async operation
            return "async-result";
        });
    
    // When
    StepResult<String> result = asyncStep.execute(context, asyncResult);
    
    // Then
    await().atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> {
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo("async-result");
        });
}
```

### Testing with Transactions

```java
@Test
@Transactional
@Rollback(false) // Keep data for debugging
void shouldHandleTransactionCorrectly() {
    // Test transaction behavior
}

@Test
@Transactional(propagation = Propagation.REQUIRES_NEW)
void shouldIsolateTransaction() {
    // Test with isolated transaction
}
```

### Testing Event Publishing

```java
@Test
void shouldPublishEventsAfterSuccess() {
    // Given
    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    
    // When
    commandService.execute(command);
    
    // Then
    verify(eventPublisher, times(2)).publish(eventCaptor.capture());
    
    List<Object> publishedEvents = eventCaptor.getAllValues();
    assertThat(publishedEvents).hasSize(2);
    assertThat(publishedEvents.get(0)).isInstanceOf(OrderCreatedEvent.class);
    assertThat(publishedEvents.get(1)).isInstanceOf(InventoryReservedEvent.class);
}
```

### Testing Error Scenarios

```java
@Test
void shouldHandleMultipleErrorScenarios() {
    // Test validation errors
    assertThatThrownBy(() -> service.execute(invalidRequest))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorType()).isEqualTo(ErrorType.VALIDATION);
            assertThat(be.getErrorCode()).isNotBlank();
        });
    
    // Test business errors
    assertThatThrownBy(() -> service.execute(businessErrorRequest))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorType()).isEqualTo(ErrorType.BUSINESS);
        });
    
    // Test system errors
    when(externalService.call(any())).thenThrow(new RuntimeException());
    
    assertThatThrownBy(() -> service.execute(validRequest))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> {
            BusinessException be = (BusinessException) ex;
            assertThat(be.getErrorType()).isEqualTo(ErrorType.SYSTEM);
        });
}
```

## Performance Testing

```java
@Test
@Timeout(value = 2, unit = TimeUnit.SECONDS)
void shouldCompleteWithinTimeout() {
    // Test must complete within 2 seconds
}

@Test
void shouldHandleLoadEfficiently() {
    // Given
    int numberOfRequests = 1000;
    List<CompletableFuture<Response>> futures = new ArrayList<>();
    
    // When
    for (int i = 0; i < numberOfRequests; i++) {
        CompletableFuture<Response> future = CompletableFuture
            .supplyAsync(() -> service.execute(createRequest(i)));
        futures.add(future);
    }
    
    // Then
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    List<Response> responses = futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
    
    assertThat(responses).hasSize(numberOfRequests);
    assertThat(responses).allMatch(Response::isSuccessful);
}
```

## Conclusion

Testing FlowStep applications is straightforward due to the framework's design:
- Clear separation of concerns enables focused unit tests
- Step-based architecture allows testing individual business logic pieces
- Context pattern simplifies mocking and verification
- Consistent error handling makes error testing predictable

Follow these testing practices to ensure your FlowStep applications are reliable, maintainable, and bug-free.