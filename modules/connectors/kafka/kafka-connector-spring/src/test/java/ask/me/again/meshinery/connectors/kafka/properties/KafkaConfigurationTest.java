package ask.me.again.meshinery.connectors.kafka.properties;

import ask.me.again.meshinery.connectors.kafka.KafkaConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
    classes = KafkaConfiguration.class,
    initializers = ConfigDataApplicationContextInitializer.class
)
class KafkaConfigurationTest {

  @Autowired
  KafkaProperties kafkaProperties;

  @Test
  void testProperties() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    assertThat(kafkaProperties)
        .extracting(
            KafkaProperties::getBootstrapServer,
            KafkaProperties::getGroupId
        )
        .containsExactly(
            "abc",
            "groupid"
        );
  }


}
