# FlowStep Examples - API Testing Guide

This guide provides comprehensive testing scenarios for both Spring Boot 2 and Spring Boot 3 examples.

## Environment Setup

### Spring Boot 2 Example
- **URL**: http://localhost:8080/flowstep-example
- **Port**: 8080
- **H2 Console**: http://localhost:8080/flowstep-example/h2-console

### Spring Boot 3 Example
- **URL**: http://localhost:8081/flowstep-example
- **Port**: 8081
- **H2 Console**: http://localhost:8081/flowstep-example/h2-console

## Test Scenarios

### 1. Multi-Step Query Operations

#### Basic User Order Summary
```bash
# Spring Boot 2
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary"

# Spring Boot 3
curl -X GET "http://localhost:8081/flowstep-example/api/orders/users/1/summary"
```

#### Query with Parameters
```bash
# With limit
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary?limit=3"

# With date range
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59"

# All parameters
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&includeInactiveOrders=false&limit=5"
```

#### Different Users
```bash
# User 2 (Jane Smith)
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/2/summary"

# User 3 (Bob Wilson)
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/3/summary"
```

### 2. Multi-Step Command Operations

#### Create Order - Simple
```bash
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

#### Create Order - Multiple Items
```bash
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "orderItems": [
      {
        "productId": 2,
        "quantity": 2
      },
      {
        "productId": 8,
        "quantity": 1,
        "unitPrice": 39.99
      },
      {
        "productId": 9,
        "quantity": 1
      }
    ]
  }'
```

#### Create Order - With Custom Pricing
```bash
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "orderItems": [
      {
        "productId": 4,
        "quantity": 1,
        "unitPrice": 139.99
      }
    ]
  }'
```

### 3. Error Handling Test Scenarios

#### Invalid User ID
```bash
# Non-existent user
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/999/summary"

# Inactive user (user ID 4)
curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/4/summary"
```

#### Invalid Product Orders
```bash
# Non-existent product
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 999,
        "quantity": 1
      }
    ]
  }'

# Inactive product (product ID 10)
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 10,
        "quantity": 1
      }
    ]
  }'

# Insufficient stock (order more than available)
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 1,
        "quantity": 100
      }
    ]
  }'
```

#### Validation Errors
```bash
# Invalid user ID
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 0,
    "orderItems": [
      {
        "productId": 1,
        "quantity": 1
      }
    ]
  }'

# Invalid quantity
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 1,
        "quantity": 0
      }
    ]
  }'

# Empty order items
curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": []
  }'
```

### 4. Concurrent Testing

#### Test Stock Depletion Race Conditions
```bash
# Run multiple concurrent orders for the same product
for i in {1..5}; do
  curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
    -H "Content-Type: application/json" \
    -d '{
      "userId": 1,
      "orderItems": [
        {
          "productId": 5,
          "quantity": 10
        }
      ]
    }' &
done
wait
```

### 5. Data Verification

#### Check Database State
Access H2 Console and run these queries:

```sql
-- View all users
SELECT * FROM users;

-- View all products with stock
SELECT id, name, price, stock_quantity, is_active FROM products ORDER BY id;

-- View all orders
SELECT * FROM orders ORDER BY created_at DESC;

-- View order items for latest orders
SELECT oi.*, p.name as product_name 
FROM order_items oi 
JOIN products p ON oi.product_id = p.id 
ORDER BY oi.created_at DESC;

-- Check stock levels after orders
SELECT id, name, stock_quantity FROM products WHERE id IN (1,2,3,4,5);
```

### 6. Performance Testing

#### Measure Query Performance
```bash
# Time the query operations
time curl -X GET "http://localhost:8080/flowstep-example/api/orders/users/1/summary"

# Time command operations
time curl -X POST "http://localhost:8080/flowstep-example/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

#### Load Testing with Apache Bench
```bash
# Install Apache Bench (ab)
# Ubuntu/Debian: sudo apt-get install apache2-utils
# macOS: brew install httpie

# Test query endpoint
ab -n 100 -c 10 "http://localhost:8080/flowstep-example/api/orders/users/1/summary"

# Test with POST (requires file)
echo '{
  "userId": 1,
  "orderItems": [
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}' > order.json

ab -n 50 -c 5 -T "application/json" -p order.json "http://localhost:8080/flowstep-example/api/orders"
```

## Expected Results

### Successful Query Response Structure
```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "isActive": true
  },
  "recentOrders": [...],
  "totalSpent": 1699.95,
  "totalOrderCount": 3,
  "averageOrderValue": 566.65,
  "mostCommonStatus": "DELIVERED",
  "topProducts": [...]
}
```

### Successful Command Response Structure
```json
{
  "order": {
    "id": 7,
    "userId": 1,
    "totalAmount": 29.99,
    "status": "PENDING",
    "orderDate": "2024-01-20T15:45:00"
  },
  "orderItems": [...],
  "message": "Order created successfully with 1 items"
}
```

### Error Response Structure
```json
{
  "timestamp": "2024-01-20T15:45:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "User not found or inactive",
  "path": "/api/orders/users/999/summary"
}
```

## Monitoring and Debugging

### Application Logs
Monitor application logs for step execution details:
```bash
# Follow logs (if running with Docker)
docker logs -f flowstep-example

# Check logs for step execution
grep "Executing.*step" application.log

# Check for errors
grep "ERROR" application.log
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/flowstep-example/actuator/health

# Detailed health information
curl http://localhost:8080/flowstep-example/actuator/health | jq '.'
```

### Metrics
```bash
# View available metrics
curl http://localhost:8080/flowstep-example/actuator/metrics

# Specific metrics
curl http://localhost:8080/flowstep-example/actuator/metrics/jvm.memory.used
```

## Test Automation Script

Here's a complete test script for automated testing:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/flowstep-example"
CONTENT_TYPE="Content-Type: application/json"

echo "Testing FlowStep Examples..."

# Test 1: Valid query
echo "Test 1: Valid user query"
curl -s -X GET "$BASE_URL/api/orders/users/1/summary" | jq '.'

# Test 2: Valid command
echo "Test 2: Valid order creation"
curl -s -X POST "$BASE_URL/api/orders" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": 1,
    "orderItems": [
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }' | jq '.'

# Test 3: Error handling
echo "Test 3: Invalid user query"
curl -s -X GET "$BASE_URL/api/orders/users/999/summary" | jq '.'

echo "All tests completed!"
```

This comprehensive testing guide covers all aspects of the FlowStep examples, from basic functionality to error handling and performance testing.