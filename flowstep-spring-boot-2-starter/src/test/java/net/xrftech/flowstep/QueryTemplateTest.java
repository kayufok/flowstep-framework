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

import java.util.Arrays;
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

        TestRequest request = new TestRequest("test-data");

        // When
        String response = queryService.execute(request);

        // Then
        assertThat(response).isEqualTo("processed: test-data");
    }

    @Test
    void shouldThrowBusinessExceptionWhenValidationFails() {
        // Given
        TestRequest invalidRequest = new TestRequest(""); // Empty data should fail validation

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

        TestRequest request = new TestRequest("test-data");

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

        TestRequest request = new TestRequest("test-data");

        // When/Then
        assertThatThrownBy(() -> queryService.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("System error during query");
    }

    // Test implementation classes
    private static class TestRequest {
        private final String data;

        TestRequest(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    private class TestQueryService extends QueryTemplate<TestRequest, String> {

        @Override
        protected StepResult<Void> validate(TestRequest request) {
            if (request.getData() == null || request.getData().trim().isEmpty()) {
                return StepResult.failure("Request data cannot be empty", "VALIDATION_ERROR", ErrorType.VALIDATION);
            }
            return StepResult.success();
        }

        @Override
        protected List<QueryStep<?>> steps(TestRequest request, QueryContext context) {
            return Arrays.asList(mockStep1, mockStep2);
        }

        @Override
        protected String buildResponse(QueryContext context) {
            return "processed: " + context.<TestRequest>get("request").getData();
        }
    }
}
