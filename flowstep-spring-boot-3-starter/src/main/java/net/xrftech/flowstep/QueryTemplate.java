package net.xrftech.flowstep;

import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.BusinessException;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Abstract template for query (read) operations.
 * 
 * Provides a skeletal implementation of the query execution flow:
 * 1. Request validation
 * 2. Context initialization
 * 3. Step execution (serial)
 * 4. Response building
 * 
 * This template enforces:
 * - No transactions (read-only operations)
 * - No side effects
 * - Pure business object returns
 * - Consistent error handling
 * - Context-based data sharing between steps
 * 
 * @param <R> the type of the query request
 * @param <S> the type of the query response
 */
@Slf4j
public abstract class QueryTemplate<R, S> {
    
    /**
     * Main execution method for query operations.
     * 
     * This method orchestrates the entire query flow and should not
     * be overridden by subclasses. The template method pattern ensures
     * consistent execution while allowing customization through abstract methods.
     * 
     * @param request the query request
     * @return the query response
     * @throws BusinessException if the query fails due to business rules
     */
    public final S execute(R request) throws BusinessException {
        log.debug("Starting query execution for request: {}", request.getClass().getSimpleName());
        
        try {
            // 1. Validate request (use default implementation or subclass override)
            var validateResult = validate(request);
            if (!validateResult.isSuccess()) {
                log.warn("Query validation failed: {}", validateResult.getMessage());
                throw new BusinessException(
                    validateResult.getErrorCode(), 
                    validateResult.getMessage(), 
                    validateResult.getErrorType()
                );
            }
            
            // 2. Create and initialize context
            var context = new QueryContext();
            context.setRequest(request);
            context.markStartTime();
            
            // 3. Execute all steps in sequence
            var stepList = steps(request, context);
            log.debug("Executing {} query steps", stepList.size());
            
            for (var i = 0; i < stepList.size(); i++) {
                var step = stepList.get(i);
                log.debug("Executing query step {}: {}", i + 1, step.getClass().getSimpleName());
                
                @SuppressWarnings("unchecked")
                var stepResult = ((QueryStep<Object>) step).execute(context);
                
                if (!stepResult.isSuccess()) {
                    log.warn("Query step {} failed: {}", i + 1, stepResult.getMessage());
                    throw new BusinessException(
                        stepResult.getErrorCode(),
                        stepResult.getMessage(),
                        stepResult.getErrorType()
                    );
                }
                
                log.debug("Query step {} completed successfully", i + 1);
            }
            
            // 4. Build and return response
            var response = buildResponse(context);
            log.debug("Query execution completed successfully in {}ms", 
                     context.getExecutionDuration());
            
            return response;
            
        } catch (BusinessException e) {
            log.error("Business exception in query execution: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in query execution", e);
            throw new BusinessException("SYS_001", "System error during query", ErrorType.SYSTEM);
        }
    }
    
    /**
     * Validates the query request.
     * 
     * Default implementation returns success. Subclasses can override
     * to provide custom validation logic.
     * 
     * @param request the query request to validate
     * @return validation result
     */
    protected StepResult<Void> validate(R request) {
        return StepResult.success();
    }
    
    /**
     * Defines the sequence of steps to execute for this query.
     * 
     * Subclasses must implement this method to define their specific
     * query logic. The method receives both the request and context
     * to enable conditional step execution based on request parameters
     * or intermediate results.
     * 
     * Steps are executed in the order they appear in the returned list.
     * 
     * @param request the query request
     * @param context the query context (already contains the request)
     * @return ordered list of steps to execute
     */
    protected abstract List<QueryStep<?>> steps(R request, QueryContext context);
    
    /**
     * Builds the query response from the context.
     * 
     * This method is called after all steps have executed successfully.
     * It should extract the necessary data from the context and build
     * the final response object.
     * 
     * @param context the query context containing step results
     * @return the query response
     */
    protected abstract S buildResponse(QueryContext context);
}
