package net.xrftech.flowstep.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Base context class providing shared storage mechanism for step execution.
 * 
 * This context allows steps to share data without tight coupling.
 * It provides a simple key-value store for intermediate results and
 * cross-step communication.
 * 
 * Thread-safe usage is not guaranteed - contexts should be used within
 * a single thread execution flow.
 */
public abstract class BaseContext {
    protected final Map<String, Object> store = new HashMap<>();

    /**
     * Stores a value in the context with the given key.
     * 
     * @param key the key to store the value under
     * @param value the value to store
     * @param <T> the type of the value
     */
    public <T> void put(String key, T value) {
        store.put(key, value);
    }

    /**
     * Retrieves a value from the context by key.
     * 
     * @param key the key to retrieve
     * @param <T> the expected type of the value
     * @return the stored value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) store.get(key);
    }

    /**
     * Checks if a key exists in the context.
     * 
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean has(String key) {
        return store.containsKey(key);
    }

    /**
     * Retrieves a value from the context, returning a default value if not found.
     * This method improves condition judgment fluency in step implementations.
     * 
     * @param key the key to retrieve
     * @param defaultValue the default value to return if key not found
     * @param <T> the expected type of the value
     * @return the stored value, or defaultValue if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        T value = (T) store.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Removes a value from the context.
     * 
     * @param key the key to remove
     * @return the removed value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T remove(String key) {
        return (T) store.remove(key);
    }

    /**
     * Clears all values from the context.
     */
    public void clear() {
        store.clear();
    }

    /**
     * Gets the number of entries in the context.
     * 
     * @return the size of the context
     */
    public int size() {
        return store.size();
    }

    /**
     * Checks if the context is empty.
     * 
     * @return true if the context has no entries
     */
    public boolean isEmpty() {
        return store.isEmpty();
    }
}
