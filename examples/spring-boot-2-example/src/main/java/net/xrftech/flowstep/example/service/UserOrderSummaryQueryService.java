package net.xrftech.flowstep.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.QueryTemplate;
import net.xrftech.flowstep.annotation.QueryFlow;
import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.UserOrderSummaryRequest;
import net.xrftech.flowstep.example.dto.UserOrderSummaryResponse;
import net.xrftech.flowstep.example.dto.UserOrderSummaryResponse.ProductSummary;
import net.xrftech.flowstep.example.model.Order;
import net.xrftech.flowstep.example.model.User;
import net.xrftech.flowstep.example.step.FetchUserStep;
import net.xrftech.flowstep.example.step.FetchUserOrdersStep;
import net.xrftech.flowstep.example.step.CalculateOrderStatisticsStep;
import net.xrftech.flowstep.example.step.FetchTopProductsStep;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Multi-step query service demonstrating FlowStep QueryTemplate usage.
 * 
 * This service orchestrates 4 distinct steps:
 * 1. Fetch user information (JPA)
 * 2. Fetch user's orders with filtering (JPA)
 * 3. Calculate order statistics (JPA aggregations)
 * 4. Fetch top products (MyBatis complex query)
 * 
 * Each step depends on data from previous steps, demonstrating
 * context-based data sharing in FlowStep operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@QueryFlow(
    code = "USER_ORDER_SUMMARY", 
    desc = "Retrieve comprehensive user order summary with statistics and top products",
    enableLogging = true,
    logLevel = QueryFlow.LogLevel.INFO,
    includeRequestResponse = true,
    includePerformanceMetrics = true,
    tags = {"user-service", "order-analytics", "business-critical"}
)
public class UserOrderSummaryQueryService extends QueryTemplate<UserOrderSummaryRequest, UserOrderSummaryResponse> {
    
    private final FetchUserStep fetchUserStep;
    private final FetchUserOrdersStep fetchUserOrdersStep;
    private final CalculateOrderStatisticsStep calculateOrderStatisticsStep;
    private final FetchTopProductsStep fetchTopProductsStep;
    
    @Override
    protected StepResult<Void> validate(UserOrderSummaryRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            return StepResult.failure("VAL_001", "User ID must be positive", ErrorType.VALIDATION);
        }
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                return StepResult.failure("VAL_002", "Start date must be before end date", ErrorType.VALIDATION);
            }
        }
        
        if (request.getLimit() != null && request.getLimit() <= 0) {
            return StepResult.failure("VAL_003", "Limit must be positive", ErrorType.VALIDATION);
        }
        
        return StepResult.success();
    }
    
    @Override
    protected List<QueryStep<?>> steps(UserOrderSummaryRequest request, QueryContext context) {
        // Define the sequence of steps to execute
        // Each step will store its results in the context for subsequent steps
        return Arrays.asList(
            fetchUserStep,              // Step 1: Fetch user info
            fetchUserOrdersStep,        // Step 2: Fetch user orders (depends on user)
            calculateOrderStatisticsStep, // Step 3: Calculate statistics (depends on user)
            fetchTopProductsStep        // Step 4: Fetch top products (depends on user)
        );
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected UserOrderSummaryResponse buildResponse(QueryContext context) {
        // Extract data from context that was populated by the steps
        User user = context.get("user", User.class);
        List<Order> userOrders = (List<Order>) context.get("userOrders");
        Map<String, Object> statistics = (Map<String, Object>) context.get("orderStatistics");
        List<ProductSummary> topProducts = (List<ProductSummary>) context.get("topProducts");
        
        // Build and return the response
        return UserOrderSummaryResponse.builder()
            .user(user)
            .recentOrders(userOrders)
            .totalSpent((BigDecimal) statistics.get("totalSpent"))
            .totalOrderCount((Integer) statistics.get("totalOrderCount"))
            .averageOrderValue((BigDecimal) statistics.get("averageOrderValue"))
            .mostCommonStatus((Order.OrderStatus) statistics.get("mostCommonStatus"))
            .topProducts(topProducts)
            .build();
    }
}