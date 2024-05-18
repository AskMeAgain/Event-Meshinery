package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MeshineryCorePropertiesTest extends AbstractLogTestBase {

  @Test
  void testProperties(OutputCapture output) {
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