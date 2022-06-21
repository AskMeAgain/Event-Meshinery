package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.system.CapturedOutput;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeshineryCorePropertiesTest extends AbstractLogTestBase {

  @Test
  void testProperties(CapturedOutput output) {
    //Arrange --------------------------------------------------------------------------------
    var application = new SpringApplication(MeshineryAutoConfiguration.class);

    //Act ------------------------------------------------------------------------------------
    assertThrows(
        RuntimeException.class,
        () -> application.run("--meshinery.core.inject=a,,b", "--spring.main.web-application-type=none")
    );

    //Assert ---------------------------------------------------------------------------------
    assertThatLogContainsMessage(
        output,
        "Property: meshinery.core.inject[1]",
        "Reason: must not be blank"
    );
  }

}