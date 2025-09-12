# FlowStep Examples

This directory contains comprehensive example projects demonstrating the FlowStep library usage with both Spring Boot 2 and Spring Boot 3.

## Overview

The FlowStep library provides a structured approach to implementing multi-step operations in Spring Boot applications. These examples showcase:

- **Multi-step Query Operations**: Complex read operations involving multiple database queries and data aggregation
- **Multi-step Command Operations**: Transactional write operations with validation, persistence, and side effects
- **Mixed Data Access**: Integration of both JPA and MyBatis within the same application
- **Error Handling**: Comprehensive error handling and validation at each step
- **Audit and Events**: Event generation for audit trails and downstream processing

## Example Projects

### [Spring Boot 2 Example](./spring-boot-2-example/)
- Uses Spring Boot 2.7.18
- Demonstrates FlowStep with javax.* annotations
- Includes comprehensive multi-step operations
- Port: 8080

### [Spring Boot 3 Example](./spring-boot-3-example/)
- Uses Spring Boot 3.2.1
- Demonstrates FlowStep with jakarta.* annotations
- Identical functionality to Spring Boot 2 example
- Port: 8081

## Key Features Demonstrated

### Multi-Step Query Operations (4 Steps)
1. **Fetch User Information** - Validate user exists and is active
2. **Fetch User Orders** - Retrieve orders with optional filtering
3. **Calculate Statistics** - Aggregate order data (totals, averages, etc.)
4. **Fetch Top Products** - Complex MyBatis query for user's favorite products

### Multi-Step Command Operations (4 Steps)
1. **Validate User** - Ensure user exists and is active
2. **Validate Products & Stock** - Check product availability and pricing
3. **Create Order** - Persist order record with JPA
4. **Create Order Items & Update Stock** - Persist order items and update inventory

## Domain Model

The examples use a simple e-commerce domain:

- **User**: Customer information
- **Product**: Product catalog with inventory
- **Order**: Customer orders with status tracking
- **OrderItem**: Individual items within orders

## Technology Stack

- **Spring Boot**: 2.7.18 / 3.2.1
- **FlowStep**: Latest version
- **Database**: H2 (in-memory, configurable to MySQL)
- **Data Access**: JPA + MyBatis
- **Build Tool**: Gradle
- **Java Version**: 11 (Spring Boot 2) / 17 (Spring Boot 3)

## Quick Start

1. **Build the projects**:
   ```bash
   ./gradlew build
   ```

2. **Run Spring Boot 2 example**:
   ```bash
   ./gradlew :examples:spring-boot-2-example:bootRun
   ```

3. **Run Spring Boot 3 example**:
   ```bash
   ./gradlew :examples:spring-boot-3-example:bootRun
   ```

4. **Access the applications**:
   - Spring Boot 2: http://localhost:8080/flowstep-example
   - Spring Boot 3: http://localhost:8081/flowstep-example
   - H2 Console: http://localhost:8080/flowstep-example/h2-console

## API Endpoints

### Query Operations
- `GET /api/orders/users/{userId}/summary` - Multi-step user order summary
- `GET /api/orders/users/{userId}/summary-simple` - Simplified version

### Command Operations
- `POST /api/orders` - Multi-step order creation

## Sample Requests

See the individual project README files for detailed API examples and sample requests.

## Learning Path

1. **Start with the Query Operation**: Understand how FlowStep orchestrates read-only operations
2. **Explore the Command Operation**: Learn about transactional multi-step operations
3. **Examine Error Handling**: See how failures are handled at each step
4. **Study the Integration**: Understand how JPA and MyBatis work together
5. **Review the Configuration**: Learn about FlowStep configuration options

## Next Steps

- Extend the examples with additional steps
- Add custom validation logic
- Implement event publishing
- Add performance monitoring
- Create custom step types

Each example project contains detailed documentation and inline comments explaining the FlowStep concepts and implementation patterns.