package io.github.askmeagain.meshinery.connectors.postgres;

import io.github.askmeagain.meshinery.connectors.mysql.MeshineryPostgresConfiguration;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgresPropertyValidationTest extends AbstractLogTestBase {

  @Test
  void testProperties(OutputCapture output) {
    //Arrange --------------------------------------------------------------------------------
    var application = new SpringApplication(MeshineryPostgresConfiguration.class);

    //Act ------------------------------------------------------------------------------------
    assertThrows(
        RuntimeException.class,
        () -> application.run(
            "--meshinery.connectors.postgres.connection-string=",
            "--spring.main.web-application-type=none"
        )
    );

    //Assert ---------------------------------------------------------------------------------
    assertThatLogContainsMessage(
        output, "Property: meshinery.connectors.postgres.connectionString", "Reason: must not be blank");
  }

}