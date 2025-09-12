package net.xrftech.flowstep.example.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * Request DTO for complex user order summary queries.
 * This demonstrates multi-step query operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderSummaryRequest {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Builder.Default
    private Boolean includeInactiveOrders = false;
    
    @Builder.Default
    private Integer limit = 10;
}