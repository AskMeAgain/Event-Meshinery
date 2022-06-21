package io.github.askmeagain.meshinery.connectors.kafka;

import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.diagnostics.LoggingFailureAnalysisReporter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MeshineryKafkaPropertyValidationTest extends AbstractLogTestBase {

  @Test
  void testProperties(CapturedOutput output) {
    //Arrange --------------------------------------------------------------------------------
    var application = new SpringApplication(MeshineryKafkaConfiguration.class);

    //Act ------------------------------------------------------------------------------------
    assertThrows(
        RuntimeException.class,
        () -> application.run("--meshinery.connectors.kafka.bootstrapServers=")
    );

    //Assert ---------------------------------------------------------------------------------
    assertThatLogContainsMessage(
        output, "Property: meshinery.connectors.kafka.bootstrapServers", "Reason: must not be blank");
  }

}
