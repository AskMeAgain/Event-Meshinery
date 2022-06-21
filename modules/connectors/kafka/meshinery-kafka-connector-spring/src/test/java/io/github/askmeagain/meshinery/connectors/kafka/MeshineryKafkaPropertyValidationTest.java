package io.github.askmeagain.meshinery.connectors.kafka;

import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.system.CapturedOutput;

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
