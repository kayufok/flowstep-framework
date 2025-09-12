package net.xrftech.flowstep.context;

/**
 * Context class specifically for query (read) operations.
 * 
 * Extends BaseContext to provide query-specific functionality.
 * This context is used throughout the query template execution
 * to share data between validation, steps, and response building.
 * 
 * Future enhancements can include:
 * - Trace ID for request tracking
 * - Query performance metrics
 * - Cache keys and strategies
 * - Read-only optimization flags
 */
public class QueryContext extends BaseContext {
    
    /**
     * Gets the original query request from context.
     * This is a convenience method since the request is commonly accessed.
     * 
     * @param <T> the type of the request
     * @return the query request
     */
    @SuppressWarnings("unchecked")
    public <T> T getRequest() {
        return (T) get("request");
    }
    
    /**
     * Sets the query request in context.
     * This is typically done by the template during initialization.
     * 
     * @param request the query request
     * @param <T> the type of the request
     */
    public <T> void setRequest(T request) {
        put("request", request);
    }
    
    // Future extensibility examples:
    
    /**
     * Sets a trace ID for request correlation across services.
     * 
     * @param traceId the trace identifier
     */
    public void setTraceId(String traceId) {
        put("traceId", traceId);
    }
    
    /**
     * Gets the trace ID for this query.
     * 
     * @return the trace identifier, or null if not set
     */
    public String getTraceId() {
        return get("traceId");
    }
    
    /**
     * Marks the start time for performance tracking.
     */
    public void markStartTime() {
        put("startTime", System.currentTimeMillis());
    }
    
    /**
     * Gets the execution duration in milliseconds.
     * 
     * @return duration since start time, or -1 if start time not set
     */
    public long getExecutionDuration() {
        Long startTime = get("startTime");
        return startTime != null ? System.currentTimeMillis() - startTime : -1;
    }
}
