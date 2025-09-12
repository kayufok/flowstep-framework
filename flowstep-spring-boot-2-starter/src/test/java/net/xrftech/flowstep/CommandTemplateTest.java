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

import java.util.Arrays;
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

        TestCommand command = new TestCommand("test-command");

        // When
        String response = commandService.execute(command);

        // Then
        assertThat(response).isEqualTo("executed: test-command");
    }

    @Test
    void shouldThrowBusinessExceptionWhenValidationFails() {
        // Given
        TestCommand invalidCommand = new TestCommand(""); // Empty action should fail validation

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

        TestCommand command = new TestCommand("test-command");

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

        TestCommand command = new TestCommand("test-command");

        // When/Then
        assertThatThrownBy(() -> commandService.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("System error during command");
    }

    // Test implementation classes
    private static class TestCommand {
        private final String action;

        TestCommand(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

    private class TestCommandService extends CommandTemplate<TestCommand, String> {

        @Override
        protected StepResult<Void> validate(TestCommand command) {
            if (command.getAction() == null || command.getAction().trim().isEmpty()) {
                return StepResult.failure("Command action cannot be empty", "VALIDATION_ERROR", ErrorType.VALIDATION);
            }
            return StepResult.success();
        }

        @Override
        protected List<CommandStep<?>> steps(TestCommand command, CommandContext context) {
            return Arrays.asList(mockStep1, mockStep2);
        }

        @Override
        protected String buildResponse(CommandContext context) {
            return "executed: " + context.<TestCommand>getCommand().getAction();
        }
    }
}
