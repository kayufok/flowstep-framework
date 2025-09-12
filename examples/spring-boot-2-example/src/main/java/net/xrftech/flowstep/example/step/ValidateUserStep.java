package net.xrftech.flowstep.example.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.StepResult;
import net.xrftech.flowstep.example.dto.CreateOrderCommand;
import net.xrftech.flowstep.example.model.User;
import net.xrftech.flowstep.example.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Command Step 1: Validate that the user exists and is active.
 * This is the first step in a multi-step command operation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateUserStep implements CommandStep<User> {
    
    private final UserRepository userRepository;
    
    @Override
    public StepResult<User> execute(CommandContext context) throws Exception {
        try {
            CreateOrderCommand command = context.getCommand();
            Long userId = command.getUserId();
            
            log.debug("Validating user with ID: {}", userId);
            
            Optional<User> userOpt = userRepository.findActiveUserById(userId);
            
            if (userOpt.isEmpty()) {
                log.warn("User not found or inactive: {}", userId);
                return StepResult.failure("USER_001", "User not found or inactive", ErrorType.BUSINESS);
            }
            
            User user = userOpt.get();
            
            // Store user in context for subsequent steps
            context.put("validatedUser", user);
            
            log.debug("Successfully validated user: {} ({})", user.getUsername(), user.getId());
            
            return StepResult.success(user);
            
        } catch (Exception e) {
            log.error("Error validating user", e);
            return StepResult.failure("USER_002", "Error validating user: " + e.getMessage(), ErrorType.SYSTEM);
        }
    }
}