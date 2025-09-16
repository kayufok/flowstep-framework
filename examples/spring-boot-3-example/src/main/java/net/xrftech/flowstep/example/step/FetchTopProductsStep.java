package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.UserOrderSummaryRequest;
import net.xrftech.flowstep.example.dto.UserOrderSummaryResponse.ProductSummary;
import net.xrftech.flowstep.example.mapper.ProductMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Step 4: Fetch user's top products using MyBatis.
 * This step demonstrates using MyBatis for complex queries within FlowStep operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FetchTopProductsStep implements QueryStep<List<ProductSummary>> {
    
    private final ProductMapper productMapper;
    
    @Override
    public StepResult<List<ProductSummary>> execute(QueryContext context) throws Exception {
        try {
            UserOrderSummaryRequest request = context.getRequest();
            Long userId = request.getUserId();
            
            log.debug("Fetching top products for user ID: {}", userId);
            
            // Use MyBatis to fetch top products with complex aggregation
            List<ProductSummary> topProducts = productMapper.findTopProductsByUser(userId, 5);
            
            // Store top products in context for response building
            context.put("topProducts", topProducts);
            
            log.debug("Successfully fetched {} top products for user {}", topProducts.size(), userId);
            
            return StepResult.success(topProducts);
            
        } catch (Exception e) {
            log.error("Error fetching top products", e);
            return StepResult.failure("PRODUCT_001", "Error fetching top products: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}