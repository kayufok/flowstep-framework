package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.CreateOrderCommand;
import net.xrftech.flowstep.example.model.Product;
import net.xrftech.flowstep.example.mapper.ProductMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command Step 2: Validate products exist, are active, and have sufficient stock.
 * Also retrieves current prices if not provided in the command.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateProductsAndStockStep implements CommandStep<Map<String, Object>> {
    
    private final ProductMapper productMapper;
    
    @Override
    public StepResult<Map<String, Object>> execute(CommandContext context) throws Exception {
        try {
            CreateOrderCommand command = context.getCommand();
            List<CreateOrderCommand.OrderItem> orderItems = command.getOrderItems();
            
            log.debug("Validating {} products and stock", orderItems.size());
            
            List<Product> validatedProducts = new ArrayList<>();
            Map<Long, CreateOrderCommand.OrderItem> itemMap = new HashMap<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (CreateOrderCommand.OrderItem item : orderItems) {
                Long productId = item.getProductId();
                Integer quantity = item.getQuantity();
                
                // Fetch product using MyBatis
                Product product = productMapper.findById(productId);
                
                if (product == null) {
                    log.warn("Product not found: {}", productId);
                    return StepResult.failure("PRODUCT_001", 
                        "Product not found: " + productId, ErrorType.BUSINESS);
                }
                
                if (!product.getIsActive()) {
                    log.warn("Product is inactive: {}", productId);
                    return StepResult.failure("PRODUCT_002", 
                        "Product is inactive: " + productId, ErrorType.BUSINESS);
                }
                
                if (product.getStockQuantity() < quantity) {
                    log.warn("Insufficient stock for product {}: requested={}, available={}", 
                             productId, quantity, product.getStockQuantity());
                    return StepResult.failure("PRODUCT_003", 
                        "Insufficient stock for product " + productId + 
                        ": requested=" + quantity + ", available=" + product.getStockQuantity(), 
                        ErrorType.BUSINESS);
                }
                
                // Use provided price or current product price
                BigDecimal unitPrice = item.getUnitPrice() != null ? 
                    item.getUnitPrice() : product.getPrice();
                
                // Update item with current price if not provided
                if (item.getUnitPrice() == null) {
                    item.setUnitPrice(unitPrice);
                }
                
                validatedProducts.add(product);
                itemMap.put(productId, item);
                
                // Calculate total
                BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(itemTotal);
            }
            
            // Store validation results in context
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("validatedProducts", validatedProducts);
            validationResult.put("orderItems", orderItems);
            validationResult.put("totalAmount", totalAmount);
            
            context.put("productValidation", validationResult);
            
            log.debug("Successfully validated {} products with total amount: {}", 
                     validatedProducts.size(), totalAmount);
            
            return StepResult.success(validationResult);
            
        } catch (Exception e) {
            log.error("Error validating products and stock", e);
            return StepResult.failure("PRODUCT_004", 
                "Error validating products and stock: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}