package net.xrftech.flowstep.example.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import net.xrftech.flowstep.example.model.Order;
import net.xrftech.flowstep.example.model.OrderItem;

import java.util.List;

/**
 * Response DTO for create order command.
 * This demonstrates the result of multi-step command operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    
    private Order order;
    
    private List<OrderItem> orderItems;
    
    private String message;
}