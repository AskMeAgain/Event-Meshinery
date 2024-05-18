package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import io.github.askmeagain.meshinery.monitoring.config.MeshineryDrawerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeshineryDrawerPropertyValidationTest extends AbstractLogTestBase {

  @Test
  void testProperties(OutputCapture output) {
    //Arrange --------------------------------------------------------------------------------
    var application = new SpringApplication(MeshineryDrawerConfiguration.class);

    //Act ------------------------------------------------------------------------------------
    assertThrows(
        RuntimeException.class,
        () -> application.run(
            "--meshinery.draw.output-format=",
            "--spring.main.web-application-type=none",
            "--meshinery.draw.grafana-dashboard-push.enabled=false"
        )
    );

    //Assert ---------------------------------------------------------------------------------
    assertThatLogContainsMessage(
        output, "Property: meshinery.draw.outputFormat", "Reason: must not be blank");
  }

}