package net.xrftech.flowstep;

import net.xrftech.flowstep.context.CommandContext;
import net.xrftech.flowstep.context.QueryContext;
import net.xrftech.flowstep.exception.BusinessException;
import net.xrftech.flowstep.step.CommandStep;
import net.xrftech.flowstep.step.QueryStep;
import net.xrftech.flowstep.step.StepResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify QueryStep can be used in CommandFlow.
 */
class CommandTemplateWithQueryStepTest {

    private TestCommandWithMixedSteps testCommand;
    
    @BeforeEach
    void setUp() {
        testCommand = new TestCommandWithMixedSteps();
    }
    
    @Test
    void shouldExecuteCommandWithMixedStepTypes() throws BusinessException {
        // Given
        TestCommand command = new TestCommand("test-data");
        
        // When
        TestResponse response = testCommand.execute(command);
        
        // Then
        assertNotNull(response);
        assertEquals("query-result", response.queryData);
        assertEquals("command-result", response.commandData);
        assertEquals("test-data-processed", response.finalResult);
    }
    
    @Test
    void shouldShareContextBetweenQueryAndCommandSteps() throws BusinessException {
        // Given
        TestCommand command = new TestCommand("shared-context-test");
        
        // When
        TestResponse response = testCommand.execute(command);
        
        // Then
        assertNotNull(response);
        // Verify that data set by QueryStep is accessible to CommandStep
        assertEquals("shared-context-test-processed", response.finalResult);
        assertEquals("query-result", response.queryData);
        assertEquals("command-result", response.commandData);
    }
    
    // Test classes
    
    static class TestCommand {
        final String data;
        
        TestCommand(String data) {
            this.data = data;
        }
    }
    
    static class TestResponse {
        final String queryData;
        final String commandData;
        final String finalResult;
        
        TestResponse(String queryData, String commandData, String finalResult) {
            this.queryData = queryData;
            this.commandData = commandData;
            this.finalResult = finalResult;
        }
    }
    
    static class TestCommandWithMixedSteps extends CommandTemplate<TestCommand, TestResponse> {
        
        // Define a QueryStep
        private final QueryStep<String> queryStep = (QueryContext context) -> {
            // Simulate read operation
            String data = "query-result";
            context.put("queryData", data);
            return StepResult.success(data);
        };
        
        // Define a CommandStep
        private final CommandStep<String> commandStep = (CommandContext context) -> {
            // Simulate write operation
            String queryData = context.get("queryData");
            String commandData = "command-result";
            context.put("commandData", commandData);
            context.put("combined", queryData + "-" + commandData);
            return StepResult.success(commandData);
        };
        
        @Override
        protected List<?> steps(TestCommand command, CommandContext context) {
            List<Object> steps = new ArrayList<>();
            
            // Add QueryStep
            steps.add(queryStep);
            
            // Add CommandStep
            steps.add(commandStep);
            
            // Add inline QueryStep
            steps.add((QueryStep<?>) (ctx) -> {
                String combined = ctx.get("combined");
                String processed = command.data + "-processed";
                ctx.put("finalResult", processed);
                return StepResult.success(processed);
            });
            
            return steps;
        }
        
        @Override
        protected TestResponse buildResponse(CommandContext context) {
            return new TestResponse(
                context.get("queryData"),
                context.get("commandData"),
                context.get("finalResult")
            );
        }
    }
}