# FlowStep Spring Boot 3 Example

This example demonstrates comprehensive usage of the FlowStep library in a Spring Boot 3 application, showcasing multi-step query and command operations with mixed data access patterns.

## Overview

This application implements a simple e-commerce system with the following multi-step operations:

### Multi-Step Query Operation (4 Steps)
**Endpoint**: `GET /api/orders/users/{userId}/summary`

1. **FetchUserStep**: Validates user exists and is active (JPA)
2. **FetchUserOrdersStep**: Retrieves user's orders with optional filtering (JPA)
3. **CalculateOrderStatisticsStep**: Calculates aggregated statistics (JPA)
4. **FetchTopProductsStep**: Retrieves user's top products (MyBatis)

### Multi-Step Command Operation (4 Steps)
**Endpoint**: `POST /api/orders`

1. **ValidateUserStep**: Ensures user exists and is active (JPA)
2. **ValidateProductsAndStockStep**: Validates products and stock levels (MyBatis)
3. **CreateOrderStep**: Creates the order record (JPA)
4. **CreateOrderItemsAndUpdateStockStep**: Creates order items and updates inventory (JPA + MyBatis)

## Technology Stack

- **Spring Boot**: 3.2.1
- **Java**: 17+
- **FlowStep**: Latest version
- **Database**: H2 (configurable to MySQL)
- **Data Access**: JPA (Hibernate) + MyBatis
- **Build Tool**: Gradle

## Key Differences from Spring Boot 2

This example is functionally identical to the Spring Boot 2 version but uses:

- **Jakarta EE**: `jakarta.*` packages instead of `javax.*`
- **Spring Boot 3**: Latest Spring Boot features and improvements
- **Java 17**: Modern Java features and performance improvements
- **Updated Dependencies**: Latest versions of all dependencies

## Project Structure

```
src/main/java/net/xrftech/flowstep/example/
├── FlowStepExampleApplication.java     # Main application class
├── controller/
│   └── OrderController.java            # REST API endpoints
├── dto/                                 # Data Transfer Objects
│   ├── CreateOrderCommand.java
│   ├── CreateOrderResponse.java
│   ├── UserOrderSummaryRequest.java
│   └── UserOrderSummaryResponse.java
├── model/                              # JPA Entities
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   └── OrderItem.java
├── repository/                         # JPA Repositories
│   ├── UserRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── mapper/                             # MyBatis Mappers
│   └── ProductMapper.java
├── service/                            # FlowStep Services
│   ├── UserOrderSummaryQueryService.java
│   └── CreateOrderCommandService.java
└── step/                              # Individual Steps
    ├── FetchUserStep.java
    ├── FetchUserOrdersStep.java
    ├── CalculateOrderStatisticsStep.java
    ├── FetchTopProductsStep.java
    ├── ValidateUserStep.java
    ├── ValidateProductsAndStockStep.java
    ├── CreateOrderStep.java
    └── CreateOrderItemsAndUpdateStockStep.java
```

## Running the Application

### Prerequisites
- Java 17 or higher
- Gradle (or use the wrapper)

### Build and Run

1. **Build the project**:
   ```bash
   ./gradlew :examples:spring-boot-3-example:build
   ```

2. **Run the application**:
   ```bash
   ./gradlew :examples:spring-boot-3-example:bootRun
   ```

3. **Access the application**:
   - Application: http://localhost:8081/flowstep-example
   - H2 Console: http://localhost:8081/flowstep-example/h2-console
   - Health Check: http://localhost:8081/flowstep-example/actuator/health

## API Examples

### 1. Multi-Step Query: Get User Order Summary

**Request**:
```bash
curl -X GET "http://localhost:8081/flowstep-example/api/orders/users/1/summary?limit=5"
```

**Response**:
```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "isActive": true
  },
  "recentOrders": [
    {
      "id": 3,
      "userId": 1,
      "totalAmount": 89.99,
      "status": "SHIPPED",
      "orderDate": "2024-01-15T10:30:00"
    }
  ],
  "totalSpent": 1699.95,
  "totalOrderCount": 3,
  "averageOrderValue": 566.65,
  "mostCommonStatus": "DELIVERED",
  "topProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro 15\"",
      "orderCount": 1,
      "totalSpent": 1299.99
    }
  ]
}
```

### 2. Multi-Step Query with Date Filtering

**Request**:
```bash
curl -X GET "http://localhost:8081/flowstep-example/api/orders/users/1/summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&limit=10"
```

### 3. Multi-Step Command: Create Order

**Request**:
```bash
curl -X POST "http://localhost:8081/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 2,
        "quantity": 2
      },
      {
        "productId": 8,
        "quantity": 1,
        "unitPrice": 39.99
      }
    ]
  }'
```

**Response**:
```json
{
  "order": {
    "id": 7,
    "userId": 1,
    "totalAmount": 99.97,
    "status": "PENDING",
    "orderDate": "2024-01-20T15:45:00"
  },
  "orderItems": [
    {
      "id": 13,
      "orderId": 7,
      "productId": 2,
      "quantity": 2,
      "unitPrice": 29.99
    },
    {
      "id": 14,
      "orderId": 7,
      "productId": 8,
      "quantity": 1,
      "unitPrice": 39.99
    }
  ],
  "message": "Order created successfully with 2 items"
}
```

## Sample Data

The application comes pre-loaded with sample data:

- **Users**: 4 users (3 active, 1 inactive)
- **Products**: 10 products (9 active, 1 inactive)
- **Orders**: 6 existing orders with various statuses
- **Order Items**: Corresponding order items

You can view the data using the H2 console at: http://localhost:8081/flowstep-example/h2-console

**Connection details**:
- JDBC URL: `jdbc:h2:mem:flowstep_demo`
- Username: `sa`
- Password: (empty)

## Spring Boot 3 Specific Features

### Jakarta EE Migration
All annotations and imports use the `jakarta.*` namespace:
```java
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
```

### Improved Performance
- **Native Image Support**: Ready for GraalVM native compilation
- **Optimized Startup**: Faster application startup times
- **Memory Efficiency**: Reduced memory footprint

### Enhanced Observability
- **Micrometer Integration**: Built-in metrics and tracing
- **Health Indicators**: Enhanced health check capabilities
- **Logging Improvements**: Better structured logging

## Configuration

### Application Configuration
The application can be configured via `application.yml`:

```yaml
# Database switching
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flowstep_demo  # Switch to MySQL
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: your_username
    password: your_password

# FlowStep configuration
flowstep:
  enabled: true
  performance:
    metrics-enabled: true
    slow-query-threshold-ms: 1000
  logging:
    step-execution: true
    context-data: false
```

### Native Image Configuration
For GraalVM native image compilation:

```bash
./gradlew :examples:spring-boot-3-example:nativeCompile
```

## Testing

### Manual Testing
1. Start the application
2. Use the provided curl commands (note port 8081)
3. Check the H2 console for data changes
4. Monitor logs for step execution details

### Error Scenarios
Test error handling by:
- Using invalid user IDs (e.g., 999)
- Requesting inactive users (e.g., user ID 4)
- Ordering products with insufficient stock
- Using invalid product IDs

### Concurrent Testing
Spring Boot 3 improvements make it ideal for testing concurrent operations:

```bash
# Test concurrent order creation
for i in {1..10}; do
  curl -X POST "http://localhost:8081/flowstep-example/api/orders" \
    -H "Content-Type: application/json" \
    -d '{"userId": 1, "orderItems": [{"productId": 2, "quantity": 1}]}' &
done
wait
```

## Migration from Spring Boot 2

If migrating from the Spring Boot 2 example:

1. **Update Imports**: Change `javax.*` to `jakarta.*`
2. **Update Dependencies**: Use Spring Boot 3 compatible versions
3. **Java Version**: Ensure Java 17+ is used
4. **Configuration**: Review configuration properties for changes
5. **Testing**: Test all functionality with the new version

## Best Practices for Spring Boot 3

1. **Use Records**: Consider using Java records for DTOs
2. **Virtual Threads**: Leverage virtual threads for improved concurrency
3. **Native Image**: Prepare for native compilation
4. **Observability**: Utilize built-in observability features
5. **Security**: Take advantage of improved security features

## Performance Considerations

Spring Boot 3 offers several performance improvements:

- **Faster Startup**: Reduced application startup time
- **Lower Memory Usage**: More efficient memory utilization
- **Better Throughput**: Improved request handling capacity
- **Native Compilation**: Option for native image compilation

## Monitoring and Observability

Enhanced monitoring capabilities:

```bash
# View metrics
curl http://localhost:8081/flowstep-example/actuator/metrics

# Health check with details
curl http://localhost:8081/flowstep-example/actuator/health

# Application info
curl http://localhost:8081/flowstep-example/actuator/info
```

## Future Enhancements

Consider these Spring Boot 3 specific enhancements:

1. **Virtual Threads**: Implement virtual thread support for better concurrency
2. **Native Image**: Create native executable for faster startup
3. **Observability**: Add custom metrics and traces
4. **Security**: Implement OAuth2 resource server
5. **GraphQL**: Add GraphQL support for flexible APIs

This example demonstrates the full power of FlowStep in a modern Spring Boot 3 environment, showcasing both the library's capabilities and Spring Boot 3's improvements.