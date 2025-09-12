package net.xrftech.flowstep.step;

import net.xrftech.flowstep.exception.ErrorType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StepResultTest {

    @Test
    void shouldCreateSuccessfulResult() {
        // When
        StepResult<String> result = StepResult.success("test-data");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("test-data");
        assertThat(result.getMessage()).isNull();
        assertThat(result.getErrorCode()).isNull();
        assertThat(result.getErrorType()).isNull();
    }

    @Test
    void shouldCreateSuccessfulResultWithoutData() {
        // When
        StepResult<Void> result = StepResult.success();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).isNull();
        assertThat(result.getErrorCode()).isNull();
        assertThat(result.getErrorType()).isNull();
    }

    @Test
    void shouldCreateFailureResult() {
        // When
        StepResult<String> result = StepResult.failure("Error occurred", "ERR_001", ErrorType.BUSINESS);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).isEqualTo("Error occurred");
        assertThat(result.getErrorCode()).isEqualTo("ERR_001");
        assertThat(result.getErrorType()).isEqualTo(ErrorType.BUSINESS);
    }

    @Test
    void shouldCreateValidationFailureResult() {
        // When
        StepResult<String> result = StepResult.validationFailure("Invalid input");

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).isEqualTo("Invalid input");
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(result.getErrorType()).isEqualTo(ErrorType.VALIDATION);
    }

    @Test
    void shouldCreateSystemFailureResult() {
        // When
        StepResult<String> result = StepResult.systemFailure("System error");

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).isEqualTo("System error");
        assertThat(result.getErrorCode()).isEqualTo("SYSTEM_ERROR");
        assertThat(result.getErrorType()).isEqualTo(ErrorType.SYSTEM);
    }

    @Test
    void shouldCreateGenericFailureResult() {
        // When
        StepResult<String> result = StepResult.failure("Business rule violation");

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getData()).isNull();
        assertThat(result.getMessage()).isEqualTo("Business rule violation");
        assertThat(result.getErrorCode()).isEqualTo("GENERIC_ERROR");
        assertThat(result.getErrorType()).isEqualTo(ErrorType.BUSINESS);
    }
}
