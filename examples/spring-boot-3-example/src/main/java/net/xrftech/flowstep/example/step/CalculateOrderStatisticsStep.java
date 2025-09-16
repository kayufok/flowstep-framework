package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.UserOrderSummaryRequest;
import net.xrftech.flowstep.example.model.Order;
import net.xrftech.flowstep.example.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Step 3: Calculate order statistics for the user.
 * This step depends on data from previous steps and performs additional database queries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CalculateOrderStatisticsStep implements QueryStep<Map<String, Object>> {
    
    private final OrderRepository orderRepository;
    
    @Override
    public StepResult<Map<String, Object>> execute(QueryContext context) throws Exception {
        try {
            UserOrderSummaryRequest request = context.getRequest();
            Long userId = request.getUserId();
            
            log.debug("Calculating order statistics for user ID: {}", userId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // Calculate total spent
            BigDecimal totalSpent = orderRepository.getTotalSpentByUser(userId);
            if (totalSpent == null) totalSpent = BigDecimal.ZERO;
            statistics.put("totalSpent", totalSpent);
            
            // Calculate total order count
            Integer totalOrderCount = orderRepository.getTotalOrderCountByUser(userId);
            if (totalOrderCount == null) totalOrderCount = 0;
            statistics.put("totalOrderCount", totalOrderCount);
            
            // Calculate average order value
            BigDecimal averageOrderValue = orderRepository.getAverageOrderValueByUser(userId);
            if (averageOrderValue == null) averageOrderValue = BigDecimal.ZERO;
            statistics.put("averageOrderValue", averageOrderValue);
            
            // Get most common order status
            Object[] statusResult = orderRepository.getMostCommonOrderStatusByUser(userId);
            Order.OrderStatus mostCommonStatus = null;
            if (statusResult != null && statusResult.length > 0) {
                try {
                    mostCommonStatus = Order.OrderStatus.valueOf(statusResult[0].toString());
                } catch (Exception e) {
                    log.warn("Could not parse most common status: {}", statusResult[0]);
                    mostCommonStatus = Order.OrderStatus.PENDING;
                }
            } else {
                mostCommonStatus = Order.OrderStatus.PENDING;
            }
            statistics.put("mostCommonStatus", mostCommonStatus);
            
            // Store statistics in context for response building
            context.put("orderStatistics", statistics);
            
            log.debug("Successfully calculated statistics for user {}: totalSpent={}, totalOrders={}, avgValue={}, commonStatus={}", 
                     userId, totalSpent, totalOrderCount, averageOrderValue, mostCommonStatus);
            
            return StepResult.success(statistics);
            
        } catch (Exception e) {
            log.error("Error calculating order statistics", e);
            return StepResult.failure("STATS_001", "Error calculating order statistics: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}