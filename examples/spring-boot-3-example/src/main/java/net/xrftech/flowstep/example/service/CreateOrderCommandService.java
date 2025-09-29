package net.xrftech.flowstep.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.CommandTemplate;
import net.xrftech.flowstep.annotation.CommandFlow;
import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.CreateOrderCommand;
import net.xrftech.flowstep.example.dto.CreateOrderResponse;
import net.xrftech.flowstep.example.model.Order;
import net.xrftech.flowstep.example.model.OrderItem;
import net.xrftech.flowstep.example.step.ValidateUserStep;
import net.xrftech.flowstep.example.step.ValidateProductsAndStockStep;
import net.xrftech.flowstep.example.step.CreateOrderStep;
import net.xrftech.flowstep.example.step.CreateOrderItemsAndUpdateStockStep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Multi-step command service demonstrating FlowStep CommandTemplate usage.
 * 
 * This service orchestrates 4 distinct steps:
 * 1. Validate user exists and is active (JPA query)
 * 2. Validate products exist, are active, and have sufficient stock (MyBatis queries)
 * 3. Create order record (JPA persistence)
 * 4. Create order items and update product stock (JPA + MyBatis operations)
 * 
 * All steps participate in the same transaction, ensuring data consistency.
 * The service demonstrates both JPA and MyBatis usage within FlowStep operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional  // Required for CommandTemplate operations
@CommandFlow(code = "CREATE_ORDER", desc = "Process new customer order with multi-step validation and persistence")
public class CreateOrderCommandService extends CommandTemplate<CreateOrderCommand, CreateOrderResponse> {
    
    private final ValidateUserStep validateUserStep;
    private final ValidateProductsAndStockStep validateProductsAndStockStep;
    private final CreateOrderStep createOrderStep;
    private final CreateOrderItemsAndUpdateStockStep createOrderItemsAndUpdateStockStep;
    
    @Override
    protected StepResult<Void> validate(CreateOrderCommand command) {
        if (command.getUserId() == null || command.getUserId() <= 0) {
            return StepResult.failure("VAL_001", "User ID must be positive", ErrorType.VALIDATION);
        }
        
        if (command.getOrderItems() == null || command.getOrderItems().isEmpty()) {
            return StepResult.failure("VAL_002", "Order items are required", ErrorType.VALIDATION);
        }
        
        // Validate each order item
        for (CreateOrderCommand.OrderItem item : command.getOrderItems()) {
            if (item.getProductId() == null || item.getProductId() <= 0) {
                return StepResult.failure("VAL_003", "Product ID must be positive", ErrorType.VALIDATION);
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                return StepResult.failure("VAL_004", "Quantity must be positive", ErrorType.VALIDATION);
            }
            
            if (item.getUnitPrice() != null && item.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return StepResult.failure("VAL_005", "Unit price must be positive if provided", ErrorType.VALIDATION);
            }
        }
        
        return StepResult.success();
    }
    
    @Override
    protected List<CommandStep<?>> steps(CreateOrderCommand command, CommandContext context) {
        // Define the sequence of steps to execute
        // Each step will store its results in the context for subsequent steps
        return Arrays.asList(
            validateUserStep,                    // Step 1: Validate user
            validateProductsAndStockStep,        // Step 2: Validate products and stock
            createOrderStep,                     // Step 3: Create order (depends on user and product validation)
            createOrderItemsAndUpdateStockStep   // Step 4: Create order items and update stock
        );
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected CreateOrderResponse buildResponse(CommandContext context) {
        // Extract data from context that was populated by the steps
        Order createdOrder = context.get("createdOrder");
        List<OrderItem> createdOrderItems = (List<OrderItem>) context.get("createdOrderItems");
        
        // Build and return the response
        return CreateOrderResponse.builder()
            .order(createdOrder)
            .orderItems(createdOrderItems)
            .message("Order created successfully with " + createdOrderItems.size() + " items")
            .build();
    }
    
    @Override
    protected void handlePostExecution(CommandContext context) {
        // Log all events for audit purposes
        List<Object> events = context.getEvents();
        log.info("Order creation completed with {} audit events", events.size());
        
        // In a real application, you might publish these events to a message queue
        // or event store for further processing
        for (Object event : events) {
            log.debug("Audit event: {}", event);
        }
        
        // Call parent implementation
        super.handlePostExecution(context);
    }
}