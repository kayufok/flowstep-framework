package net.xrftech.flowstep.context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Context class specifically for command (write) operations.
 * 
 * Extends BaseContext to provide command-specific functionality.
 * This context is used throughout the command template execution
 * to share data between validation, steps, and response building.
 * 
 * Includes built-in support for:
 * - Audit information tracking
 * - User context and permissions
 * - Transaction metadata
 * - Event publishing support
 */
public class CommandContext extends BaseContext {
    
    /**
     * Gets the original command from context.
     * This is a convenience method since the command is commonly accessed.
     * 
     * @param <T> the type of the command
     * @return the command object
     */
    @SuppressWarnings("unchecked")
    public <T> T getCommand() {
        return (T) get("command");
    }
    
    /**
     * Sets the command in context.
     * This is typically done by the template during initialization.
     * 
     * @param command the command object
     * @param <T> the type of the command
     */
    public <T> void setCommand(T command) {
        put("command", command);
    }
    
    // Audit Information Support
    
    /**
     * Sets the user ID who initiated this command.
     * 
     * @param userId the user identifier
     */
    public void setUserId(String userId) {
        put("audit.userId", userId);
    }
    
    /**
     * Gets the user ID who initiated this command.
     * 
     * @return the user identifier, or null if not set
     */
    public String getUserId() {
        return get("audit.userId");
    }
    
    /**
     * Sets the timestamp when this command was initiated.
     * 
     * @param timestamp the command initiation timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        put("audit.timestamp", timestamp);
    }
    
    /**
     * Gets the command initiation timestamp.
     * 
     * @return the timestamp, or null if not set
     */
    public LocalDateTime getTimestamp() {
        return get("audit.timestamp");
    }
    
    /**
     * Sets the client/system that initiated this command.
     * 
     * @param source the command source identifier
     */
    public void setSource(String source) {
        put("audit.source", source);
    }
    
    /**
     * Gets the command source identifier.
     * 
     * @return the source identifier, or null if not set
     */
    public String getSource() {
        return get("audit.source");
    }
    
    /**
     * Adds audit information in bulk.
     * 
     * @param auditInfo map containing audit key-value pairs
     */
    public void addAuditInfo(Map<String, Object> auditInfo) {
        auditInfo.forEach((key, value) -> put("audit." + key, value));
    }
    
    /**
     * Gets all audit information as a map.
     * 
     * @return map of audit information
     */
    public Map<String, Object> getAuditInfo() {
        Map<String, Object> auditInfo = new HashMap<>();
        store.entrySet().stream()
             .filter(entry -> entry.getKey().startsWith("audit."))
             .forEach(entry -> auditInfo.put(entry.getKey().substring(6), entry.getValue()));
        return auditInfo;
    }
    
    // Event Publishing Support
    
    /**
     * Adds an event to be published after successful command execution.
     * 
     * @param event the event object to publish
     */
    public void addEvent(Object event) {
        java.util.List<Object> events = getOrDefault("events", new java.util.ArrayList<>());
        events.add(event);
        put("events", events);
    }
    
    /**
     * Gets all events to be published.
     * 
     * @return list of events
     */
    public java.util.List<Object> getEvents() {
        return getOrDefault("events", new java.util.ArrayList<>());
    }
    
    // Transaction Support
    
    /**
     * Sets a transaction ID for correlation.
     * 
     * @param transactionId the transaction identifier
     */
    public void setTransactionId(String transactionId) {
        put("transactionId", transactionId);
    }
    
    /**
     * Gets the transaction ID.
     * 
     * @return the transaction identifier, or null if not set
     */
    public String getTransactionId() {
        return get("transactionId");
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
