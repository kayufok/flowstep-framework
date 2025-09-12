package net.xrftech.flowstep;

import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.BusinessException;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryTemplateTest {

    @Mock
    private QueryStep<String> mockStep1;

    @Mock
    private QueryStep<String> mockStep2;

    private final TestQueryService queryService = new TestQueryService();

    @Test
    void shouldExecuteSuccessfulQueryFlow() throws Exception {
        // Given
        when(mockStep1.execute(any(QueryContext.class)))
                .thenReturn(StepResult.success("step1-result"));
        when(mockStep2.execute(any(QueryContext.class)))
                .thenReturn(StepResult.success("step2-result"));

        var request = new TestRequest("test-data");

        // When
        var response = queryService.execute(request);

        // Then
        assertThat(response).isEqualTo("processed: test-data");
    }

    @Test
    void shouldThrowBusinessExceptionWhenValidationFails() {
        // Given
        var invalidRequest = new TestRequest(""); // Empty data should fail validation

        // When/Then
        assertThatThrownBy(() -> queryService.execute(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Request data cannot be empty");
    }

    @Test
    void shouldThrowBusinessExceptionWhenStepFails() throws Exception {
        // Given
        when(mockStep1.execute(any(QueryContext.class)))
                .thenReturn(StepResult.failure("Step failed", "STEP_ERROR", ErrorType.BUSINESS));

        var request = new TestRequest("test-data");

        // When/Then
        assertThatThrownBy(() -> queryService.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Step failed");
    }

    @Test
    void shouldConvertUnexpectedExceptionToSystemError() throws Exception {
        // Given
        when(mockStep1.execute(any(QueryContext.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        var request = new TestRequest("test-data");

        // When/Then
        assertThatThrownBy(() -> queryService.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("System error during query");
    }

    // Test implementation classes using modern Java features
    
    /**
     * Test request record demonstrating immutable request objects
     * with built-in validation and modern Java syntax.
     */
    private record TestRequest(String data) {
        public TestRequest {
            // Compact constructor with validation
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null");
            }
        }
        
        public boolean isValid() {
            return !data.trim().isEmpty();
        }
    }
    
    /**
     * Test response record for query results
     */
    private record TestResponse(String result, long processingTimeMs) {
        public static TestResponse of(String result) {
            return new TestResponse(result, System.currentTimeMillis());
        }
    }

    private class TestQueryService extends QueryTemplate<TestRequest, String> {

        @Override
        protected StepResult<Void> validate(TestRequest request) {
            return request.isValid()
                ? StepResult.success()
                : StepResult.failure("Request data cannot be empty", "VALIDATION_ERROR", ErrorType.VALIDATION);
        }

        @Override
        protected List<QueryStep<?>> steps(TestRequest request, QueryContext context) {
            return List.of(mockStep1, mockStep2);
        }

        @Override
        protected String buildResponse(QueryContext context) {
            var request = context.<TestRequest>getRequest();
            return "processed: " + request.data();
        }
    }
}
