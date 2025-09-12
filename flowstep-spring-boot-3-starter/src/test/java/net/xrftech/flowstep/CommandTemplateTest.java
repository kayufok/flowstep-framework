package net.xrftech.flowstep;

import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.exception.BusinessException;
import net.xrftech.flowstep.exception.ErrorType;
import net.xrftech.flowstep.step.CommandStep;
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
class CommandTemplateTest {

    @Mock
    private CommandStep<String> mockStep1;

    @Mock
    private CommandStep<String> mockStep2;

    private final TestCommandService commandService = new TestCommandService();

    @Test
    void shouldExecuteSuccessfulCommandFlow() throws Exception {
        // Given
        when(mockStep1.execute(any(CommandContext.class)))
                .thenReturn(StepResult.success("step1-result"));
        when(mockStep2.execute(any(CommandContext.class)))
                .thenReturn(StepResult.success("step2-result"));

        var command = new TestCommand("test-command");

        // When
        var response = commandService.execute(command);

        // Then
        assertThat(response).isEqualTo("executed: test-command");
    }

    @Test
    void shouldThrowBusinessExceptionWhenValidationFails() {
        // Given
        var invalidCommand = new TestCommand(""); // Empty action should fail validation

        // When/Then
        assertThatThrownBy(() -> commandService.execute(invalidCommand))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Command action cannot be empty");
    }

    @Test
    void shouldThrowBusinessExceptionWhenStepFails() throws Exception {
        // Given
        when(mockStep1.execute(any(CommandContext.class)))
                .thenReturn(StepResult.failure("Step failed", "STEP_ERROR", ErrorType.BUSINESS));

        var command = new TestCommand("test-command");

        // When/Then
        assertThatThrownBy(() -> commandService.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Step failed");
    }

    @Test
    void shouldConvertUnexpectedExceptionToSystemError() throws Exception {
        // Given
        when(mockStep1.execute(any(CommandContext.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        var command = new TestCommand("test-command");

        // When/Then
        assertThatThrownBy(() -> commandService.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("System error during command");
    }

    // Test implementation classes using modern Java features
    
    /**
     * Test command record demonstrating immutable command objects
     * with built-in validation and modern Java syntax.
     */
    private record TestCommand(String action) {
        public TestCommand {
            // Compact constructor with validation
            if (action == null) {
                throw new IllegalArgumentException("Action cannot be null");
            }
        }
        
        public boolean isValid() {
            return !action.trim().isEmpty();
        }
    }

    private class TestCommandService extends CommandTemplate<TestCommand, String> {

        @Override
        protected StepResult<Void> validate(TestCommand command) {
            return command.isValid()
                ? StepResult.success()
                : StepResult.failure("Command action cannot be empty", "VALIDATION_ERROR", ErrorType.VALIDATION);
        }

        @Override
        protected List<CommandStep<?>> steps(TestCommand command, CommandContext context) {
            return List.of(mockStep1, mockStep2);
        }

        @Override
        protected String buildResponse(CommandContext context) {
            var command = context.<TestCommand>getCommand();
            return "executed: " + command.action();
        }
    }
}
