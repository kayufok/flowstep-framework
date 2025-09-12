package net.xrftech.flowstep.step;

import net.xrftech.flowstep.context.CommandContext;

/**
 * Functional interface for command (write) operation steps.
 * 
 * CommandSteps are stateless, functional components that perform
 * write operations such as:
 * - Database inserts/updates/deletes
 * - External API calls with side effects
 * - Event publishing
 * - File operations
 * - Cache invalidation
 * - Audit logging
 * 
 * Steps participate in the transactional context established
 * by the CommandTemplate and should be designed to work
 * within Spring's transaction management.
 * 
 * @param <T> the type of data this step produces
 */
@FunctionalInterface
public interface CommandStep<T> {
    
    /**
     * Executes this command step.
     * 
     * The step should:
     * - Read any required input from the context
     * - Perform its write operation
     * - Store results in the context for other steps
     * - Add audit information if needed
     * - Return a StepResult indicating success/failure
     * 
     * Steps should handle their own exceptions and convert them
     * to appropriate StepResult failure responses. Any exceptions
     * that escape will cause the entire command to fail and
     * trigger transaction rollback.
     * 
     * @param context the command context containing shared data and audit info
     * @return the result of this step's execution
     * @throws Exception if an unrecoverable error occurs
     */
    StepResult<T> execute(CommandContext context) throws Exception;
}
