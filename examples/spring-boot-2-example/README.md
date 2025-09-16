# FlowStep Spring Boot 2 Example

This example demonstrates comprehensive usage of the FlowStep library in a Spring Boot 2 application, showcasing multi-step query and command operations with mixed data access patterns.

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

- **Spring Boot**: 2.7.18
- **Java**: 11+
- **FlowStep**: Latest version
- **Database**: H2 (configurable to MySQL)
- **Data Access**: JPA (Hibernate) + MyBatis
- **Build Tool**: Gradle

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
- Java 11 or higher
- Gradle (or use the wrapper)

### Build and Run

1. **Build the project**:
   ```bash
   ./gradlew :examples:spring-boot-2-example:build
   ```

2. **Run the application**:
   ```bash
   ./gradlew :examples:spring-boot-2-example:bootRun
   ```

3. **Access the application**:
   - Application: http://localhost:8080/flowstep-example
   - H2 Console: http://localhost:8080/flowstep-example/h2-console
   - Health Check: http://localhost:8080/flowstep-example/actuator/health

## API Examples

### 1. Multi-Step Query: Get User Order Summary

**Request**:
```bash
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary?limit=5"
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
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&limit=10"
```

### 3. Multi-Step Command: Create Order

**Request**:
```bash
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
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

You can view the data using the H2 console at: http://localhost:8080/flowstep-example/h2-console

**Connection details**:
- JDBC URL: `jdbc:h2:mem:flowstep_demo`
- Username: `sa`
- Password: (empty)

## FlowStep Concepts Demonstrated

### 1. QueryTemplate Usage
- **Validation**: Request validation before execution
- **Step Orchestration**: Sequential execution of read-only steps
- **Context Sharing**: Data sharing between steps via QueryContext
- **Response Building**: Aggregating step results into final response

### 2. CommandTemplate Usage
- **Transactional Execution**: All steps in a single transaction
- **Validation**: Command validation before execution
- **Context Management**: Sharing data between command steps
- **Event Generation**: Audit events for downstream processing
- **Error Handling**: Automatic rollback on step failure

### 3. Mixed Data Access
- **JPA Usage**: Entity management, relationships, and JPQL queries
- **MyBatis Usage**: Complex queries, stored procedures, and fine-grained control
- **Integration**: Seamless integration of both approaches in the same application

### 4. Error Handling
- **Step-Level Errors**: Each step handles its own exceptions
- **Business Logic Errors**: Domain-specific error codes and messages
- **System Errors**: Infrastructure and unexpected errors
- **Validation Errors**: Input validation with detailed messages

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

### Logging Configuration
Detailed logging is configured for debugging:
- FlowStep operations: DEBUG level
- SQL queries: Visible in console
- Transaction management: DEBUG level

## Testing

### Manual Testing
1. Start the application
2. Use the provided curl commands
3. Check the H2 console for data changes
4. Monitor logs for step execution details

### Error Scenarios
Test error handling by:
- Using invalid user IDs (e.g., 999)
- Requesting inactive users (e.g., user ID 4)
- Ordering products with insufficient stock
- Using invalid product IDs

## Extending the Example

### Adding New Steps
1. Create a new step class implementing `QueryStep<T>` or `CommandStep<T>`
2. Add business logic in the `execute()` method
3. Update the service to include the new step
4. Test the enhanced operation

### Adding New Operations
1. Create new request/response DTOs
2. Create new steps for the operation
3. Create a new service extending `QueryTemplate` or `CommandTemplate`
4. Add controller endpoints
5. Test the new operation

## Best Practices Demonstrated

1. **Separation of Concerns**: Each step has a single responsibility
2. **Error Handling**: Comprehensive error handling at each level
3. **Data Validation**: Input validation and business rule enforcement
4. **Transaction Management**: Proper transaction boundaries for commands
5. **Logging**: Detailed logging for debugging and monitoring
6. **Configuration**: Externalized configuration for different environments
7. **Documentation**: Comprehensive inline documentation and examples

This example serves as a complete reference implementation for using FlowStep in Spring Boot 2 applications.