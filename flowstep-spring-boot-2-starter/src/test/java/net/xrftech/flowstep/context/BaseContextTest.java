package net.xrftech.flowstep.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseContextTest {

    private final QueryContext context = new QueryContext();  // Use concrete implementation

    @Test
    void shouldStoreAndRetrieveValues() {
        // Given
        String key = "testKey";
        String value = "testValue";

        // When
        context.put(key, value);

        // Then
        assertThat(context.<String>get(key)).isEqualTo(value);
    }

    @Test
    void shouldReturnNullForNonExistentKey() {
        // When
        String result = context.get("nonExistentKey");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnDefaultValueWhenKeyNotFound() {
        // Given
        String defaultValue = "default";

        // When
        String result = context.getOrDefault("nonExistentKey", defaultValue);

        // Then
        assertThat(result).isEqualTo(defaultValue);
    }

    @Test
    void shouldReturnStoredValueWhenKeyExists() {
        // Given
        String key = "existingKey";
        String storedValue = "storedValue";
        String defaultValue = "defaultValue";
        context.put(key, storedValue);

        // When
        String result = context.getOrDefault(key, defaultValue);

        // Then
        assertThat(result).isEqualTo(storedValue);
    }

    @Test
    void shouldCheckIfKeyExists() {
        // Given
        String existingKey = "existing";
        String nonExistingKey = "nonExisting";
        context.put(existingKey, "value");

        // Then
        assertThat(context.has(existingKey)).isTrue();
        assertThat(context.has(nonExistingKey)).isFalse();
    }

    @Test
    void shouldRemoveValues() {
        // Given
        String key = "keyToRemove";
        String value = "value";
        context.put(key, value);

        // When
        String removedValue = context.remove(key);

        // Then
        assertThat(removedValue).isEqualTo(value);
        assertThat(context.has(key)).isFalse();
    }

    @Test
    void shouldReturnNullWhenRemovingNonExistentKey() {
        // When
        String result = context.remove("nonExistentKey");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldClearAllData() {
        // Given
        context.put("key1", "value1");
        context.put("key2", "value2");

        // When
        context.clear();

        // Then
        assertThat(context.has("key1")).isFalse();
        assertThat(context.has("key2")).isFalse();
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        String key = "nullKey";

        // When
        context.put(key, null);

        // Then
        assertThat(context.has(key)).isTrue();
        assertThat((Object) context.get(key)).isNull();  // Cast to resolve ambiguity
    }
}
