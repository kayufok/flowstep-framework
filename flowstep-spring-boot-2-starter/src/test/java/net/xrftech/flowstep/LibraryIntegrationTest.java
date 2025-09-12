package net.xrftech.flowstep;

import net.xrftech.flowstep.config.FlowStepAutoConfiguration;
import net.xrftech.flowstep.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for FlowStep Framework auto-configuration
 */
class LibraryIntegrationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FlowStepAutoConfiguration.class,
                    WebMvcAutoConfiguration.class
            ));

    @Test
    void shouldAutoConfigureGlobalExceptionHandler() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                    assertThat(context.getBean(GlobalExceptionHandler.class))
                            .isNotNull()
                            .isInstanceOf(GlobalExceptionHandler.class);
                });
    }

    @Test
    void shouldNotOverrideUserDefinedGlobalExceptionHandler() {
        contextRunner
                .withBean("customGlobalExceptionHandler", GlobalExceptionHandler.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
                    assertThat(context).hasBean("customGlobalExceptionHandler");
                });
    }

    @Test
    void shouldLoadAutoConfiguration() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(FlowStepAutoConfiguration.class);
                });
    }
}
