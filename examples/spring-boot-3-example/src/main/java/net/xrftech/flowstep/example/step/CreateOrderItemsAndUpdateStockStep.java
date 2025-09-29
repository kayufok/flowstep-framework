package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.CreateOrderCommand;
import net.xrftech.flowstep.example.model.Order;
import net.xrftech.flowstep.example.model.OrderItem;
import net.xrftech.flowstep.example.repository.OrderItemRepository;
import net.xrftech.flowstep.example.mapper.ProductMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command Step 4: Create order items and update product stock.
 * This step demonstrates both JPA (order items) and MyBatis (stock updates) operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateOrderItemsAndUpdateStockStep implements CommandStep<List<OrderItem>> {
    
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;
    
    @Override
    @SuppressWarnings("unchecked")
    public StepResult<List<OrderItem>> execute(CommandContext context) throws Exception {
        try {
            Order createdOrder = context.get("createdOrder");
            Map<String, Object> productValidation = (Map<String, Object>) context.get("productValidation");
            List<CreateOrderCommand.OrderItem> orderItems = 
                (List<CreateOrderCommand.OrderItem>) productValidation.get("orderItems");
            
            log.debug("Creating {} order items for order {}", orderItems.size(), createdOrder.getId());
            
            List<OrderItem> createdOrderItems = new ArrayList<>();
            
            for (CreateOrderCommand.OrderItem item : orderItems) {
                // Create order item entity
                OrderItem orderItem = OrderItem.builder()
                    .orderId(createdOrder.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .createdAt(LocalDateTime.now())
                    .build();
                
                // Save order item using JPA
                OrderItem savedOrderItem = orderItemRepository.save(orderItem);
                createdOrderItems.add(savedOrderItem);
                
                // Update product stock using MyBatis
                int updatedRows = productMapper.decreaseStock(item.getProductId(), item.getQuantity());
                
                if (updatedRows == 0) {
                    log.error("Failed to update stock for product {}. Possible race condition.", 
                             item.getProductId());
                    return StepResult.failure("STOCK_001", 
                        "Failed to update stock for product " + item.getProductId() + 
                        ". Stock may have been depleted by another transaction.", 
                        ErrorType.BUSINESS);
                }
                
                log.debug("Updated stock for product {} by -{} units", 
                         item.getProductId(), item.getQuantity());
            }
            
            // Store order items in context
            context.put("createdOrderItems", createdOrderItems);
            
            // Add audit events
            for (OrderItem item : createdOrderItems) {
                context.addEvent(Map.of(
                    "type", "ORDER_ITEM_CREATED",
                    "orderItemId", item.getId(),
                    "orderId", item.getOrderId(),
                    "productId", item.getProductId(),
                    "quantity", item.getQuantity()
                ));
                
                context.addEvent(Map.of(
                    "type", "STOCK_UPDATED",
                    "productId", item.getProductId(),
                    "quantityDecrease", item.getQuantity(),
                    "orderId", item.getOrderId()
                ));
            }
            
            log.debug("Successfully created {} order items and updated stock for order {}", 
                     createdOrderItems.size(), createdOrder.getId());
            
            return StepResult.success(createdOrderItems);
            
        } catch (Exception e) {
            log.error("Error creating order items and updating stock", e);
            return StepResult.failure("ORDER_ITEM_001", 
                "Error creating order items and updating stock: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}