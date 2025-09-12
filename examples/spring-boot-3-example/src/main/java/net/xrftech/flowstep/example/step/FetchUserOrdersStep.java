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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Step 2: Fetch user's recent orders with optional date filtering.
 * This step depends on the user information from Step 1.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FetchUserOrdersStep implements QueryStep<List<Order>> {
    
    private final OrderRepository orderRepository;
    
    @Override
    public StepResult<List<Order>> execute(QueryContext context) throws Exception {
        try {
            UserOrderSummaryRequest request = context.getRequest();
            Long userId = request.getUserId();
            
            log.debug("Fetching orders for user ID: {}", userId);
            
            List<Order> orders;
            
            if (request.getStartDate() != null && request.getEndDate() != null) {
                // Fetch orders within date range
                log.debug("Fetching orders between {} and {}", request.getStartDate(), request.getEndDate());
                orders = orderRepository.findUserOrdersInDateRange(
                    userId, 
                    request.getStartDate(), 
                    request.getEndDate()
                );
            } else {
                // Fetch all orders for user
                orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
            }
            
            // Apply limit if specified
            if (request.getLimit() != null && orders.size() > request.getLimit()) {
                orders = orders.subList(0, request.getLimit());
            }
            
            // Store orders in context for subsequent steps
            context.put("userOrders", orders);
            
            log.debug("Successfully fetched {} orders for user {}", orders.size(), userId);
            
            return StepResult.success(orders);
            
        } catch (Exception e) {
            log.error("Error fetching user orders", e);
            return StepResult.failure("ORDER_001", "Error fetching user orders: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}