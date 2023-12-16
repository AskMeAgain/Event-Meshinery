package io.github.askmeagain.meshinery.connectors.pubsub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
    classes = MeshineryPubSubConfiguration.class,
    initializers = ConfigDataApplicationContextInitializer.class
)
class PubSubConfigurationTest {

  @Autowired
  MeshineryPubSubProperties meshineryPubSubProperties;

  @Test
  void smokeTest() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    assertThat(meshineryPubSubProperties)
        .extracting(
            MeshineryPubSubProperties::getProjectId,
            MeshineryPubSubProperties::getLimit
        )
        .containsExactly(
            "test",
            1
        );
  }
}
