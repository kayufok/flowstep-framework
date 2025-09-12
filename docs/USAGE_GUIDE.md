# 📘 FlowStep Usage Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Basic Query Implementation](#basic-query-implementation)
3. [Basic Command Implementation](#basic-command-implementation)
4. [Advanced Patterns](#advanced-patterns)
5. [Real-World Examples](#real-world-examples)
6. [Common Patterns](#common-patterns)
7. [Error Handling](#error-handling)
8. [Testing Your Services](#testing-your-services)

## Getting Started

### Installation

#### For Spring Boot 2.7.x (Java 8+)
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### For Spring Boot 3.x (Java 17+)
```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Basic Configuration

```yaml
# application.yml
flowstep:
  enabled: true
  exception-handler:
    enabled: true
    include-stack-trace: false  # Set to true for development
```

## Basic Query Implementation

### Simple Query Example

```java
// 1. Define your request and response DTOs
public record ProductRequest(Long productId) {}

public record ProductResponse(
    Long id,
    String name,
    BigDecimal price,
    Integer stock,
    List<String> categories
) {}

// 2. Implement the query service
@QueryFlow(code = "GET_PRODUCT", desc = "Retrieve product details")
@Service
public class GetProductQuery extends QueryTemplate<ProductRequest, ProductResponse> {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public GetProductQuery(ProductRepository productRepository, 
                           CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    protected StepResult<Void> validate(ProductRequest request) {
        if (request.productId() == null || request.productId() <= 0) {
            return StepResult.validationFailure("Product ID must be positive");
        }
        return StepResult.success();
    }
    
    @Override
    protected List<QueryStep<?>> steps(ProductRequest request, QueryContext context) {
        return List.of(
            // Step 1: Fetch product
            () -> {
                Product product = productRepository.findById(request.productId())
                    .orElse(null);
                    
                if (product == null) {
                    return StepResult.failure(
                        "Product not found",
                        "PROD_404",
                        ErrorType.BUSINESS
                    );
                }
                
                context.put("product", product);
                return StepResult.success(product);
            },
            
            // Step 2: Fetch categories
            () -> {
                Product product = context.get("product");
                List<Category> categories = categoryRepository
                    .findByProductId(product.getId());
                    
                List<String> categoryNames = categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
                    
                context.put("categories", categoryNames);
                return StepResult.success(categoryNames);
            }
        );
    }
    
    @Override
    protected ProductResponse buildResponse(QueryContext context) {
        Product product = context.get("product");
        List<String> categories = context.get("categories");
        
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            categories
        );
    }
}

// 3. Use in your controller
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final GetProductQuery getProductQuery;
    
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) throws BusinessException {
        return getProductQuery.execute(new ProductRequest(id));
    }
}
```

## Basic Command Implementation

### Simple Command Example

```java
// 1. Define command and response
public record CreateProductCommand(
    String name,
    String description,
    BigDecimal price,
    Integer initialStock
) {}

public record CreateProductResponse(
    Long productId,
    String productCode,
    LocalDateTime createdAt
) {}

// 2. Implement the command service
@CommandFlow(code = "CREATE_PRODUCT", desc = "Create new product")
@Service
@Transactional
public class CreateProductService extends CommandTemplate<CreateProductCommand, CreateProductResponse> {
    
    private final ProductRepository productRepository;
    private final ProductCodeGenerator codeGenerator;
    private final EventPublisher eventPublisher;
    
    @Override
    protected StepResult<Void> validate(CreateProductCommand command) {
        // Validate command
        if (command.name() == null || command.name().trim().isEmpty()) {
            return StepResult.validationFailure("Product name is required");
        }
        
        if (command.price() == null || command.price().compareTo(BigDecimal.ZERO) <= 0) {
            return StepResult.validationFailure("Price must be positive");
        }
        
        if (command.initialStock() == null || command.initialStock() < 0) {
            return StepResult.validationFailure("Stock cannot be negative");
        }
        
        return StepResult.success();
    }
    
    @Override
    protected void initializeContext(CommandContext context, CreateProductCommand command) {
        // Add audit information
        context.setUserId(SecurityUtils.getCurrentUserId());
        context.setTimestamp(LocalDateTime.now());
        context.put("clientIp", RequestUtils.getClientIp());
    }
    
    @Override
    protected List<CommandStep<?>> steps(CreateProductCommand command, CommandContext context) {
        return List.of(
            // Step 1: Check for duplicate names
            () -> {
                boolean exists = productRepository.existsByName(command.name());
                if (exists) {
                    return StepResult.failure(
                        "Product with this name already exists",
                        "PROD_DUPLICATE",
                        ErrorType.BUSINESS
                    );
                }
                return StepResult.success();
            },
            
            // Step 2: Generate product code
            () -> {
                String productCode = codeGenerator.generateProductCode(command.name());
                context.put("productCode", productCode);
                return StepResult.success(productCode);
            },
            
            // Step 3: Create and save product
            () -> {
                Product product = Product.builder()
                    .name(command.name())
                    .description(command.description())
                    .price(command.price())
                    .stock(command.initialStock())
                    .productCode(context.get("productCode"))
                    .createdBy(context.getUserId())
                    .createdAt(context.getTimestamp())
                    .build();
                
                Product saved = productRepository.save(product);
                context.put("product", saved);
                
                // Add event for publishing
                context.addEvent(new ProductCreatedEvent(
                    saved.getId(),
                    saved.getProductCode(),
                    saved.getName()
                ));
                
                return StepResult.success(saved);
            }
        );
    }
    
    @Override
    protected CreateProductResponse buildResponse(CommandContext context) {
        Product product = context.get("product");
        
        return new CreateProductResponse(
            product.getId(),
            product.getProductCode(),
            product.getCreatedAt()
        );
    }
    
    @Override
    protected void handlePostExecution(CommandContext context) {
        // Publish events
        for (Object event : context.getEvents()) {
            eventPublisher.publish(event);
        }
        
        // Log audit
        log.info("Product created by user {} from IP {}", 
                context.getUserId(), 
                context.get("clientIp"));
    }
}

// 3. Use in controller
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final CreateProductService createProductService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateProductResponse createProduct(@RequestBody @Valid CreateProductCommand command) 
            throws BusinessException {
        return createProductService.execute(command);
    }
}
```

## Advanced Patterns

### Conditional Step Execution

```java
@Override
protected List<CommandStep<?>> steps(OrderCommand command, CommandContext context) {
    List<CommandStep<?>> steps = new ArrayList<>();
    
    // Always validate inventory
    steps.add(this::validateInventory);
    
    // Conditional discount application
    if (command.hasDiscountCode()) {
        steps.add(this::applyDiscount);
    }
    
    // Conditional shipping calculation
    if (command.requiresShipping()) {
        steps.add(this::calculateShipping);
    }
    
    // Always create order
    steps.add(this::createOrder);
    
    // Conditional notification
    if (command.sendNotification()) {
        steps.add(this::sendOrderNotification);
    }
    
    return steps;
}

private StepResult<Void> applyDiscount(CommandContext context) {
    OrderCommand command = context.getCommand();
    Discount discount = discountService.validate(command.getDiscountCode());
    
    if (discount == null) {
        return StepResult.failure("Invalid discount code", "DSC_001", ErrorType.BUSINESS);
    }
    
    BigDecimal discountAmount = calculateDiscount(context.get("subtotal"), discount);
    context.put("discount", discountAmount);
    context.put("discountCode", command.getDiscountCode());
    
    return StepResult.success();
}
```

### Parallel Step Execution (Advanced)

```java
@Override
protected List<QueryStep<?>> steps(UserProfileRequest request, QueryContext context) {
    return List.of(
        // Step 1: Fetch user (required for next steps)
        () -> {
            User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            context.put("user", user);
            return StepResult.success(user);
        },
        
        // Step 2: Parallel fetches using CompletableFuture
        () -> {
            User user = context.get("user");
            
            CompletableFuture<List<Order>> ordersFuture = CompletableFuture
                .supplyAsync(() -> orderRepository.findByUserId(user.getId()));
                
            CompletableFuture<List<Address>> addressesFuture = CompletableFuture
                .supplyAsync(() -> addressRepository.findByUserId(user.getId()));
                
            CompletableFuture<UserPreferences> preferencesFuture = CompletableFuture
                .supplyAsync(() -> preferencesRepository.findByUserId(user.getId()));
            
            try {
                // Wait for all to complete
                CompletableFuture.allOf(ordersFuture, addressesFuture, preferencesFuture).join();
                
                context.put("orders", ordersFuture.get());
                context.put("addresses", addressesFuture.get());
                context.put("preferences", preferencesFuture.get());
                
                return StepResult.success();
            } catch (Exception e) {
                return StepResult.systemFailure("Failed to fetch user data: " + e.getMessage());
            }
        }
    );
}
```

### Retry Logic in Steps

```java
private StepResult<PaymentResult> processPayment(CommandContext context) {
    Order order = context.get("order");
    PaymentDetails payment = context.get("paymentDetails");
    
    int maxRetries = 3;
    int attempt = 0;
    Exception lastException = null;
    
    while (attempt < maxRetries) {
        try {
            PaymentResult result = paymentGateway.charge(payment, order.getTotal());
            
            if (result.isSuccessful()) {
                context.put("paymentResult", result);
                context.put("transactionId", result.getTransactionId());
                return StepResult.success(result);
            } else {
                return StepResult.failure(
                    "Payment declined: " + result.getDeclineReason(),
                    "PAY_DECLINED",
                    ErrorType.BUSINESS
                );
            }
        } catch (PaymentGatewayException e) {
            lastException = e;
            attempt++;
            
            if (attempt < maxRetries) {
                log.warn("Payment attempt {} failed, retrying...", attempt);
                try {
                    Thread.sleep(1000 * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    log.error("Payment failed after {} attempts", maxRetries, lastException);
    return StepResult.systemFailure("Payment gateway unavailable");
}
```

## Real-World Examples

### E-Commerce Order Processing

```java
@CommandFlow(code = "PROCESS_ORDER", desc = "Complete order processing workflow")
@Service
@Transactional
public class ProcessOrderCommand extends CommandTemplate<OrderRequest, OrderResponse> {
    
    @Override
    protected List<CommandStep<?>> steps(OrderRequest request, CommandContext context) {
        return List.of(
            this::validateCustomer,
            this::validateProducts,
            this::checkInventory,
            this::calculatePricing,
            this::applyPromotions,
            this::processPayment,
            this::reserveInventory,
            this::createOrder,
            this::scheduleShipment,
            this::sendConfirmation
        );
    }
    
    private StepResult<Void> validateCustomer(CommandContext context) {
        OrderRequest request = context.getCommand();
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElse(null);
            
        if (customer == null) {
            return StepResult.failure("Customer not found", "CUST_404", ErrorType.BUSINESS);
        }
        
        if (customer.isBlocked()) {
            return StepResult.failure("Customer account blocked", "CUST_BLOCKED", ErrorType.BUSINESS);
        }
        
        context.put("customer", customer);
        return StepResult.success();
    }
    
    private StepResult<Void> checkInventory(CommandContext context) {
        OrderRequest request = context.getCommand();
        List<OrderItem> items = request.getItems();
        
        Map<Long, Integer> availability = new HashMap<>();
        List<String> unavailable = new ArrayList<>();
        
        for (OrderItem item : items) {
            int available = inventoryService.getAvailableQuantity(item.getProductId());
            
            if (available < item.getQuantity()) {
                unavailable.add(String.format("Product %d: requested %d, available %d",
                    item.getProductId(), item.getQuantity(), available));
            } else {
                availability.put(item.getProductId(), item.getQuantity());
            }
        }
        
        if (!unavailable.isEmpty()) {
            return StepResult.failure(
                "Insufficient inventory: " + String.join(", ", unavailable),
                "INV_INSUFFICIENT",
                ErrorType.BUSINESS
            );
        }
        
        context.put("inventoryReservation", availability);
        return StepResult.success();
    }
    
    private StepResult<Void> processPayment(CommandContext context) {
        BigDecimal total = context.get("orderTotal");
        Customer customer = context.get("customer");
        
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .customerId(customer.getId())
            .amount(total)
            .currency("USD")
            .paymentMethod(context.getCommand().getPaymentMethod())
            .build();
        
        try {
            PaymentResult result = paymentService.processPayment(paymentRequest);
            
            if (!result.isSuccessful()) {
                return StepResult.failure(
                    "Payment failed: " + result.getReason(),
                    "PAY_FAILED",
                    ErrorType.BUSINESS
                );
            }
            
            context.put("paymentConfirmation", result.getConfirmationNumber());
            context.addEvent(new PaymentProcessedEvent(customer.getId(), total, result.getTransactionId()));
            
            return StepResult.success();
            
        } catch (PaymentException e) {
            log.error("Payment processing error", e);
            return StepResult.systemFailure("Payment system error");
        }
    }
    
    @Override
    protected OrderResponse buildResponse(CommandContext context) {
        Order order = context.get("order");
        String paymentConfirmation = context.get("paymentConfirmation");
        ShipmentSchedule shipment = context.get("shipmentSchedule");
        
        return OrderResponse.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .total(order.getTotal())
            .paymentConfirmation(paymentConfirmation)
            .estimatedDelivery(shipment.getEstimatedDelivery())
            .trackingNumber(shipment.getTrackingNumber())
            .build();
    }
}
```

### User Registration with Email Verification

```java
@CommandFlow(code = "REGISTER_USER", desc = "User registration with email verification")
@Service
@Transactional
public class RegisterUserCommand extends CommandTemplate<RegistrationRequest, RegistrationResponse> {
    
    @Override
    protected StepResult<Void> validate(RegistrationRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (!EmailValidator.isValid(request.getEmail())) {
            errors.add("Invalid email format");
        }
        
        if (!PasswordValidator.isStrong(request.getPassword())) {
            errors.add("Password must be at least 8 characters with mixed case and numbers");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            errors.add("Passwords do not match");
        }
        
        if (!errors.isEmpty()) {
            return StepResult.validationFailure(String.join("; ", errors));
        }
        
        return StepResult.success();
    }
    
    @Override
    protected List<CommandStep<?>> steps(RegistrationRequest request, CommandContext context) {
        return List.of(
            // Check if email already exists
            () -> {
                boolean exists = userRepository.existsByEmail(request.getEmail());
                if (exists) {
                    return StepResult.failure(
                        "Email already registered",
                        "USER_EXISTS",
                        ErrorType.BUSINESS
                    );
                }
                return StepResult.success();
            },
            
            // Create user account
            () -> {
                String hashedPassword = passwordEncoder.encode(request.getPassword());
                String verificationToken = UUID.randomUUID().toString();
                
                User user = User.builder()
                    .email(request.getEmail())
                    .username(request.getUsername())
                    .password(hashedPassword)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .status(UserStatus.PENDING_VERIFICATION)
                    .verificationToken(verificationToken)
                    .createdAt(LocalDateTime.now())
                    .build();
                
                User saved = userRepository.save(user);
                context.put("user", saved);
                context.put("verificationToken", verificationToken);
                
                return StepResult.success(saved);
            },
            
            // Create default preferences
            () -> {
                User user = context.get("user");
                
                UserPreferences preferences = UserPreferences.builder()
                    .userId(user.getId())
                    .language("en")
                    .timezone("UTC")
                    .emailNotifications(true)
                    .theme("light")
                    .build();
                
                preferencesRepository.save(preferences);
                context.put("preferences", preferences);
                
                return StepResult.success();
            },
            
            // Send verification email
            () -> {
                User user = context.get("user");
                String token = context.get("verificationToken");
                
                EmailTemplate email = EmailTemplate.builder()
                    .to(user.getEmail())
                    .subject("Verify your account")
                    .template("email-verification")
                    .variables(Map.of(
                        "firstName", user.getFirstName(),
                        "verificationLink", buildVerificationLink(token)
                    ))
                    .build();
                
                try {
                    emailService.send(email);
                    context.addEvent(new UserRegisteredEvent(user.getId(), user.getEmail()));
                    return StepResult.success();
                } catch (EmailException e) {
                    log.error("Failed to send verification email", e);
                    // Don't fail registration if email fails
                    context.put("emailFailed", true);
                    return StepResult.success();
                }
            }
        );
    }
    
    @Override
    protected RegistrationResponse buildResponse(CommandContext context) {
        User user = context.get("user");
        boolean emailFailed = context.getOrDefault("emailFailed", false);
        
        return RegistrationResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .status(user.getStatus().toString())
            .message(emailFailed 
                ? "Registration successful. Please contact support for email verification."
                : "Registration successful. Please check your email to verify your account.")
            .build();
    }
}
```

## Common Patterns

### Pagination in Queries

```java
@QueryFlow(code = "LIST_PRODUCTS", desc = "List products with pagination")
@Service
public class ListProductsQuery extends QueryTemplate<PageRequest, PageResponse<ProductDto>> {
    
    @Override
    protected List<QueryStep<?>> steps(PageRequest request, QueryContext context) {
        return List.of(
            // Get total count
            () -> {
                long total = productRepository.count();
                context.put("totalElements", total);
                return StepResult.success(total);
            },
            
            // Get page of products
            () -> {
                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                    request.getPage(),
                    request.getSize(),
                    Sort.by(request.getSortBy()).descending()
                );
                
                Page<Product> page = productRepository.findAll(pageable);
                context.put("products", page.getContent());
                context.put("totalPages", page.getTotalPages());
                
                return StepResult.success(page);
            }
        );
    }
    
    @Override
    protected PageResponse<ProductDto> buildResponse(QueryContext context) {
        List<Product> products = context.get("products");
        Long totalElements = context.get("totalElements");
        Integer totalPages = context.get("totalPages");
        
        List<ProductDto> dtos = products.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        return PageResponse.<ProductDto>builder()
            .content(dtos)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .build();
    }
}
```

### Batch Operations

```java
@CommandFlow(code = "BULK_UPDATE", desc = "Bulk update products")
@Service
@Transactional
public class BulkUpdateProductsCommand extends CommandTemplate<BulkUpdateRequest, BulkUpdateResponse> {
    
    @Override
    protected List<CommandStep<?>> steps(BulkUpdateRequest request, CommandContext context) {
        return List.of(
            // Validate all products exist
            () -> {
                List<Long> ids = request.getUpdates().stream()
                    .map(UpdateRequest::getProductId)
                    .collect(Collectors.toList());
                
                List<Product> products = productRepository.findAllById(ids);
                
                if (products.size() != ids.size()) {
                    Set<Long> foundIds = products.stream()
                        .map(Product::getId)
                        .collect(Collectors.toSet());
                    
                    List<Long> missingIds = ids.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                    
                    return StepResult.failure(
                        "Products not found: " + missingIds,
                        "PROD_NOT_FOUND",
                        ErrorType.BUSINESS
                    );
                }
                
                context.put("products", products);
                return StepResult.success();
            },
            
            // Perform bulk update
            () -> {
                List<Product> products = context.get("products");
                Map<Long, UpdateRequest> updateMap = request.getUpdates().stream()
                    .collect(Collectors.toMap(UpdateRequest::getProductId, Function.identity()));
                
                List<Product> updated = new ArrayList<>();
                List<String> errors = new ArrayList<>();
                
                for (Product product : products) {
                    UpdateRequest update = updateMap.get(product.getId());
                    
                    try {
                        if (update.getPrice() != null) {
                            product.setPrice(update.getPrice());
                        }
                        if (update.getStock() != null) {
                            product.setStock(update.getStock());
                        }
                        if (update.getDescription() != null) {
                            product.setDescription(update.getDescription());
                        }
                        
                        product.setUpdatedAt(LocalDateTime.now());
                        product.setUpdatedBy(context.getUserId());
                        
                        updated.add(product);
                    } catch (Exception e) {
                        errors.add("Failed to update product " + product.getId() + ": " + e.getMessage());
                    }
                }
                
                if (!errors.isEmpty()) {
                    context.put("errors", errors);
                }
                
                List<Product> saved = productRepository.saveAll(updated);
                context.put("updatedProducts", saved);
                
                // Add audit event
                context.addEvent(new BulkUpdateEvent(
                    saved.size(),
                    context.getUserId(),
                    LocalDateTime.now()
                ));
                
                return StepResult.success(saved);
            }
        );
    }
    
    @Override
    protected BulkUpdateResponse buildResponse(CommandContext context) {
        List<Product> updated = context.get("updatedProducts");
        List<String> errors = context.getOrDefault("errors", Collections.emptyList());
        
        return BulkUpdateResponse.builder()
            .successCount(updated.size())
            .failureCount(errors.size())
            .errors(errors)
            .updatedIds(updated.stream().map(Product::getId).collect(Collectors.toList()))
            .build();
    }
}
```

## Error Handling

### Custom Error Handling

```java
@Service
public class CustomErrorQuery extends QueryTemplate<Request, Response> {
    
    @Override
    protected List<QueryStep<?>> steps(Request request, QueryContext context) {
        return List.of(
            () -> {
                try {
                    // External service call
                    ExternalResponse response = externalService.call(request.getData());
                    context.put("externalData", response);
                    return StepResult.success(response);
                    
                } catch (ExternalServiceException e) {
                    // Handle specific external service errors
                    if (e.isRetryable()) {
                        return StepResult.systemFailure("External service temporarily unavailable");
                    } else if (e.isClientError()) {
                        return StepResult.failure(
                            "Invalid request to external service: " + e.getMessage(),
                            "EXT_CLIENT_ERROR",
                            ErrorType.VALIDATION
                        );
                    } else {
                        return StepResult.failure(
                            "External service error: " + e.getMessage(),
                            "EXT_SERVICE_ERROR",
                            ErrorType.SYSTEM
                        );
                    }
                } catch (Exception e) {
                    log.error("Unexpected error calling external service", e);
                    return StepResult.systemFailure("Unexpected error occurred");
                }
            }
        );
    }
}
```

### Global Exception Handler Customization

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Override FlowStep's handler
public class CustomGlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomErrorResponse> handleBusinessException(
            BusinessException ex, 
            HttpServletRequest request) {
        
        CustomErrorResponse response = CustomErrorResponse.builder()
            .errorCode(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .traceId(MDC.get("traceId")) // Add trace ID from MDC
            .build();
        
        HttpStatus status = mapErrorTypeToStatus(ex.getErrorType());
        
        // Custom logging
        if (ex.getErrorType() == ErrorType.SYSTEM) {
            alertingService.sendAlert("System error: " + ex.getMessage());
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    private HttpStatus mapErrorTypeToStatus(ErrorType errorType) {
        return switch (errorType) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS -> HttpStatus.CONFLICT;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
```

## Testing Your Services

### Unit Testing Steps

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderCommandTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private InventoryService inventoryService;
    
    @InjectMocks
    private CreateOrderCommand createOrderCommand;
    
    @Test
    void shouldValidateOrderSuccessfully() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(123L)
            .items(List.of(new OrderItem(1L, 2)))
            .build();
        
        // When
        StepResult<Void> result = createOrderCommand.validate(request);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
    }
    
    @Test
    void shouldFailValidationWhenItemsEmpty() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(123L)
            .items(Collections.emptyList())
            .build();
        
        // When
        StepResult<Void> result = createOrderCommand.validate(request);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("items required");
    }
    
    @Test
    void shouldCheckInventorySuccessfully() {
        // Given
        CommandContext context = new CommandContext();
        context.setCommand(createValidRequest());
        
        when(inventoryService.checkAvailability(anyList())).thenReturn(true);
        
        // When
        StepResult<Void> result = createOrderCommand.checkInventory(context);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(inventoryService).checkAvailability(anyList());
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private CreateOrderCommand createOrderCommand;
    
    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
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
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void shouldHandleBusinessExceptionProperly() throws Exception {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(999L) // Non-existent customer
            .items(List.of(new OrderItem(1L, 2)))
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("CUST_404"))
            .andExpect(jsonPath("$.message").value("Customer not found"));
    }
}
```

## Tips and Best Practices

### 1. Step Granularity
- Keep steps focused on single responsibility
- Aim for 3-7 steps per service
- Extract complex logic into private methods

### 2. Context Management
```java
// Good: Clear, meaningful keys
context.put("validatedCustomer", customer);
context.put("orderSubtotal", subtotal);
context.put("appliedDiscounts", discounts);

// Bad: Unclear keys
context.put("c", customer);
context.put("data", someData);
context.put("temp", tempValue);
```

### 3. Error Messages
```java
// Good: Specific, actionable error messages
return StepResult.failure(
    "Product SKU-12345 has insufficient stock. Available: 5, Requested: 10",
    "STOCK_INSUFFICIENT",
    ErrorType.BUSINESS
);

// Bad: Generic error messages
return StepResult.failure("Error", "ERR_001", ErrorType.BUSINESS);
```

### 4. Transaction Boundaries
```java
// Good: Short transaction
@Transactional
public class CreateOrderCommand extends CommandTemplate<...> {
    // Quick database operations only
}

// Bad: Long transaction with external calls
@Transactional
public class ProcessOrderCommand extends CommandTemplate<...> {
    // Avoid external API calls within transaction
    // Move them to separate non-transactional steps
}
```

### 5. Event Publishing
```java
// Good: Domain events with necessary context
context.addEvent(new OrderCreatedEvent(
    orderId,
    customerId,
    orderTotal,
    LocalDateTime.now()
));

// Bad: Generic events without context
context.addEvent(new Event("order_created"));
```

## Conclusion

FlowStep provides a powerful, consistent framework for building Spring Boot applications. By following these patterns and examples, you can create maintainable, testable, and scalable services that adhere to CQRS principles and clean architecture practices.