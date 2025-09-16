package net.xrftech.flowstep.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.example.dto.CreateOrderCommand;
import net.xrftech.flowstep.example.dto.CreateOrderResponse;
import net.xrftech.flowstep.example.dto.UserOrderSummaryRequest;
import net.xrftech.flowstep.example.dto.UserOrderSummaryResponse;
import net.xrftech.flowstep.example.service.CreateOrderCommandService;
import net.xrftech.flowstep.example.service.UserOrderSummaryQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * REST Controller demonstrating FlowStep usage in Spring Boot 2.
 * 
 * This controller exposes endpoints for:
 * - Multi-step query operations (user order summary)
 * - Multi-step command operations (create order)
 * 
 * Each endpoint demonstrates different aspects of FlowStep:
 * - Query operations: read-only, multi-step data aggregation
 * - Command operations: transactional, multi-step data modification
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final UserOrderSummaryQueryService userOrderSummaryQueryService;
    private final CreateOrderCommandService createOrderCommandService;
    
    /**
     * Multi-step query endpoint: Get comprehensive user order summary.
     * 
     * This endpoint demonstrates a 4-step query operation:
     * 1. Fetch user information
     * 2. Fetch user's orders with filtering
     * 3. Calculate order statistics
     * 4. Fetch top products
     */
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<UserOrderSummaryResponse> getUserOrderSummary(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactiveOrders,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        log.info("Fetching order summary for user {} with filters: startDate={}, endDate={}, includeInactive={}, limit={}", 
                 userId, startDate, endDate, includeInactiveOrders, limit);
        
        UserOrderSummaryRequest request = UserOrderSummaryRequest.builder()
            .userId(userId)
            .startDate(startDate)
            .endDate(endDate)
            .includeInactiveOrders(includeInactiveOrders)
            .limit(limit)
            .build();
        
        UserOrderSummaryResponse response = userOrderSummaryQueryService.execute(request);
        
        log.info("Successfully retrieved order summary for user {} with {} recent orders", 
                 userId, response.getRecentOrders().size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Multi-step command endpoint: Create a new order.
     * 
     * This endpoint demonstrates a 4-step command operation:
     * 1. Validate user exists and is active
     * 2. Validate products exist, are active, and have sufficient stock
     * 3. Create order record
     * 4. Create order items and update product stock
     * 
     * All operations are transactional and will rollback on any failure.
     */
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        
        log.info("Creating order for user {} with {} items", 
                 command.getUserId(), command.getOrderItems().size());
        
        CreateOrderResponse response = createOrderCommandService.execute(command);
        
        log.info("Successfully created order {} for user {} with total amount: {}", 
                 response.getOrder().getId(), 
                 response.getOrder().getUserId(),
                 response.getOrder().getTotalAmount());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Simple endpoint to demonstrate error handling in FlowStep operations.
     */
    @GetMapping("/users/{userId}/summary-simple")
    public ResponseEntity<UserOrderSummaryResponse> getUserOrderSummarySimple(@PathVariable Long userId) {
        
        UserOrderSummaryRequest request = UserOrderSummaryRequest.builder()
            .userId(userId)
            .limit(5)
            .build();
        
        UserOrderSummaryResponse response = userOrderSummaryQueryService.execute(request);
        
        return ResponseEntity.ok(response);
    }
}