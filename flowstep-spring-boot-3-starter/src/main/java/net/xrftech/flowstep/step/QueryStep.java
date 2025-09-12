package net.xrftech.flowstep.step;

import net.xrftech.flowstep.context.QueryContext;

/**
 * Functional interface for query (read) operation steps.
 * 
 * QuerySteps are stateless, functional components that perform
 * read-only operations such as:
 * - Database queries
 * - External API calls
 * - Data transformation
 * - Conditional logic
 * - Caching operations
 * 
 * Steps should not have side effects and should be idempotent.
 * They communicate through the QueryContext and return results
 * via StepResult.
 * 
 * @param <T> the type of data this step produces
 */
@FunctionalInterface
public interface QueryStep<T> {
    
    /**
     * Executes this query step.
     * 
     * The step should:
     * - Read any required input from the context
     * - Perform its read-only operation
     * - Store results in the context for other steps
     * - Return a StepResult indicating success/failure
     * 
     * Steps should handle their own exceptions and convert them
     * to appropriate StepResult failure responses.
     * 
     * @param context the query context containing shared data
     * @return the result of this step's execution
     * @throws Exception if an unrecoverable error occurs
     */
    StepResult<T> execute(QueryContext context) throws Exception;
}
