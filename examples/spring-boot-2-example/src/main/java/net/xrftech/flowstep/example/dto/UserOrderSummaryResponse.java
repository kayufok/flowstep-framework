package net.xrftech.flowstep.example.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import net.xrftech.flowstep.example.model.User;
import net.xrftech.flowstep.example.model.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for complex user order summary queries.
 * This demonstrates the result of multi-step operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderSummaryResponse {
    
    private User user;
    
    private List<Order> recentOrders;
    
    private BigDecimal totalSpent;
    
    private Integer totalOrderCount;
    
    private BigDecimal averageOrderValue;
    
    private Order.OrderStatus mostCommonStatus;
    
    private List<ProductSummary> topProducts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private Long productId;
        private String productName;
        private Integer orderCount;
        private BigDecimal totalSpent;
    }
}