package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.CreateOrderCommand;
import net.xrftech.flowstep.example.model.Order;
import net.xrftech.flowstep.example.model.User;
import net.xrftech.flowstep.example.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Command Step 3: Create the order record in the database using JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateOrderStep implements CommandStep<Order> {
    
    private final OrderRepository orderRepository;
    
    @Override
    @SuppressWarnings("unchecked")
    public StepResult<Order> execute(CommandContext context) throws Exception {
        try {
            CreateOrderCommand command = context.getCommand();
            User validatedUser = context.get("validatedUser");
            Map<String, Object> productValidation = (Map<String, Object>) context.get("productValidation");
            BigDecimal totalAmount = (BigDecimal) productValidation.get("totalAmount");
            
            log.debug("Creating order for user {} with total amount: {}", 
                     validatedUser.getId(), totalAmount);
            
            // Create order entity
            Order order = Order.builder()
                .userId(validatedUser.getId())
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .build();
            
            // Save order using JPA
            Order savedOrder = orderRepository.save(order);
            
            // Store order in context for subsequent steps
            context.put("createdOrder", savedOrder);
            
            // Add audit event
            Map<String, Object> orderEvent = new HashMap<>();
            orderEvent.put("type", "ORDER_CREATED");
            orderEvent.put(
                "orderId", savedOrder.getId());
            orderEvent.put("userId", validatedUser.getId());
            orderEvent.put("totalAmount", totalAmount);
            context.addEvent(orderEvent);
            
            log.debug("Successfully created order with ID: {}", savedOrder.getId());
            
            return StepResult.success(savedOrder);
            
        } catch (Exception e) {
            log.error("Error creating order", e);
            return StepResult.failure("ORDER_001", 
                "Error creating order: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}