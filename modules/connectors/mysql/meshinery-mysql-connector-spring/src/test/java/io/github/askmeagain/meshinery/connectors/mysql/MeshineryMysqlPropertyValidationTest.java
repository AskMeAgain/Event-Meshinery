package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.system.CapturedOutput;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeshineryMysqlPropertyValidationTest extends AbstractLogTestBase {

  @Test
  void testProperties(CapturedOutput output) {
    //Arrange --------------------------------------------------------------------------------
    var application = new SpringApplication(MeshineryMysqlConfiguration.class);

    //Act ------------------------------------------------------------------------------------
    assertThrows(
        RuntimeException.class,
        () -> application.run(
            "--meshinery.connectors.mysql.connection-string=",
            "--spring.main.web-application-type=none"
        )
    );

    //Assert ---------------------------------------------------------------------------------
    assertThatLogContainsMessage(
        output, "Property: meshinery.connectors.mysql.connectionString", "Reason: must not be blank");
  }

}