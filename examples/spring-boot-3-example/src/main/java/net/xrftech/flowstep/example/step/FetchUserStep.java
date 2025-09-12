package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.UserOrderSummaryRequest;
import net.xrftech.flowstep.example.model.User;
import net.xrftech.flowstep.example.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Step 1: Fetch user information from the database.
 * This is the first step in a multi-step query operation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FetchUserStep implements QueryStep<User> {
    
    private final UserRepository userRepository;
    
    @Override
    public StepResult<User> execute(QueryContext context) throws Exception {
        try {
            UserOrderSummaryRequest request = context.getRequest();
            Long userId = request.getUserId();
            
            log.debug("Fetching user with ID: {}", userId);
            
            Optional<User> userOpt = userRepository.findActiveUserById(userId);
            
            if (userOpt.isEmpty()) {
                log.warn("User not found or inactive: {}", userId);
                return StepResult.failure("USER_001", "User not found or inactive", ErrorType.BUSINESS);
            }
            
            User user = userOpt.get();
            
            // Store user in context for subsequent steps
            context.put("user", user);
            
            log.debug("Successfully fetched user: {} ({})", user.getUsername(), user.getId());
            
            return StepResult.success(user);
            
        } catch (Exception e) {
            log.error("Error fetching user", e);
            return StepResult.failure("USER_002", "Error fetching user: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}