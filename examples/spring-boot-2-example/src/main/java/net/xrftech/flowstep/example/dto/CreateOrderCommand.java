package net.xrftech.flowstep.example.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

/**
 * Command DTO for creating orders with products.
 * This demonstrates multi-step command operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotEmpty(message = "Order items are required")
    private List<OrderItem> orderItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal unitPrice; // Optional - will be fetched if not provided
    }
}