package net.xrftech.flowstep;

import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.BusinessException;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract template for command (write) operations.
 * 
 * Provides a skeletal implementation of the command execution flow:
 * 1. Command validation
 * 2. Context initialization with audit information
 * 3. Step execution (serial, within transaction)
 * 4. Response building
 * 
 * This template enforces:
 * - Transactional execution (must be annotated with @Transactional)
 * - Audit information tracking
 * - Consistent error handling
 * - Context-based data sharing between steps
 * - Support for event publishing
 * 
 * @param <C> the type of the command
 * @param <R> the type of the command response
 */
@Slf4j
public abstract class CommandTemplate<C, R> {
    
    /**
     * Main execution method for command operations.
     * 
     * This method orchestrates the entire command flow and should not
     * be overridden by subclasses. The template method pattern ensures
     * consistent execution while allowing customization through abstract methods.
     * 
     * Note: The concrete service class must be annotated with @Transactional
     * to ensure proper transaction management.
     * 
     * @param command the command to execute
     * @return the command response
     * @throws BusinessException if the command fails due to business rules
     */
    public final R execute(C command) throws BusinessException {
        log.debug("Starting command execution for: {}", command.getClass().getSimpleName());
        
        try {
            // 1. Validate command (use default implementation or subclass override)
            var validateResult = validate(command);
            if (!validateResult.isSuccess()) {
                log.warn("Command validation failed: {}", validateResult.getMessage());
                throw new BusinessException(
                    validateResult.getErrorCode(),
                    validateResult.getMessage(),
                    validateResult.getErrorType()
                );
            }
            
            // 2. Create and initialize context with audit information
            var context = new CommandContext();
            context.setCommand(command);
            context.setTimestamp(LocalDateTime.now());
            context.markStartTime();
            
            // Allow subclasses to add additional context initialization
            initializeContext(context, command);
            
            // 3. Execute all steps in sequence
            var stepList = steps(command, context);
            log.debug("Executing {} command steps", stepList.size());
            
            for (var i = 0; i < stepList.size(); i++) {
                var step = stepList.get(i);
                log.debug("Executing command step {}: {}", i + 1, step.getClass().getSimpleName());
                
                @SuppressWarnings("unchecked")
                var stepResult = ((CommandStep<Object>) step).execute(context);
                
                if (!stepResult.isSuccess()) {
                    log.warn("Command step {} failed: {}", i + 1, stepResult.getMessage());
                    throw new BusinessException(
                        stepResult.getErrorCode(),
                        stepResult.getMessage(),
                        stepResult.getErrorType()
                    );
                }
                
                log.debug("Command step {} completed successfully", i + 1);
            }
            
            // 4. Build response
            var response = buildResponse(context);
            
            // 5. Handle post-execution tasks (events, notifications, etc.)
            handlePostExecution(context);
            
            log.debug("Command execution completed successfully in {}ms", 
                     context.getExecutionDuration());
            
            return response;
            
        } catch (BusinessException e) {
            log.error("Business exception in command execution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in command execution", e);
            throw new BusinessException("SYS_001", "System error during command", ErrorType.SYSTEM);
        }
    }
    
    /**
     * Validates the command.
     * 
     * Default implementation returns success. Subclasses can override
     * to provide custom validation logic.
     * 
     * @param command the command to validate
     * @return validation result
     */
    protected StepResult<Void> validate(C command) {
        return StepResult.success();
    }
    
    /**
     * Initializes the command context with additional information.
     * 
     * Default implementation does nothing. Subclasses can override
     * to add user context, permissions, or other audit information.
     * 
     * @param context the command context
     * @param command the command being executed
     */
    protected void initializeContext(CommandContext context, C command) {
        // Default implementation - subclasses can override
    }
    
    /**
     * Defines the sequence of steps to execute for this command.
     * 
     * Subclasses must implement this method to define their specific
     * command logic. The method receives both the command and context
     * to enable conditional step execution based on command parameters
     * or intermediate results.
     * 
     * Steps are executed in the order they appear in the returned list.
     * All steps participate in the same transaction.
     * 
     * @param command the command to execute
     * @param context the command context (already contains the command)
     * @return ordered list of steps to execute
     */
    protected abstract List<CommandStep<?>> steps(C command, CommandContext context);
    
    /**
     * Builds the command response from the context.
     * 
     * This method is called after all steps have executed successfully.
     * It should extract the necessary data from the context and build
     * the final response object.
     * 
     * @param context the command context containing step results
     * @return the command response
     */
    protected abstract R buildResponse(CommandContext context);
    
    /**
     * Handles post-execution tasks like event publishing.
     * 
     * Default implementation logs events but doesn't publish them.
     * Subclasses should override to integrate with their event system.
     * 
     * This method is called after successful command execution but
     * before transaction commit, allowing events to be published
     * as part of the same transaction.
     * 
     * @param context the command context containing events and audit info
     */
    protected void handlePostExecution(CommandContext context) {
        var events = context.getEvents();
        if (!events.isEmpty()) {
            log.debug("Command generated {} events for publishing", events.size());
            // Subclasses should override to actually publish events
        }
        
        // Log audit information
        if (log.isDebugEnabled()) {
            log.debug("Command audit info: {}", context.getAuditInfo());
        }
    }
}
