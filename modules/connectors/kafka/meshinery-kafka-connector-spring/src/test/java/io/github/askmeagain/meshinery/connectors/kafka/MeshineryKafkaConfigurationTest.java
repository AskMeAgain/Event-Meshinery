package io.github.askmeagain.meshinery.connectors.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
    classes = MeshineryKafkaConfiguration.class,
    initializers = ConfigDataApplicationContextInitializer.class
)
class MeshineryKafkaConfigurationTest {

  @Autowired
  MeshineryKafkaProperties meshineryKafkaProperties;

  @Test
  void testProperties() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    assertThat(meshineryKafkaProperties)
        .extracting(
            MeshineryKafkaProperties::getBootstrapServers,
            MeshineryKafkaProperties::getGroupId
        ).containsExactly(
            "abc",
            "groupid"
        );
    assertThat(meshineryKafkaProperties)
        .extracting(MeshineryKafkaProperties::getConsumerProperties)
        .extracting(
            x -> x.getProperty("test.test1"),
            x -> x.getProperty("test.test2")
        ).containsExactly("abc1", "abc2");
    assertThat(meshineryKafkaProperties)
        .extracting(MeshineryKafkaProperties::getProducerProperties)
        .extracting(
            x -> x.getProperty("test.test3"),
            x -> x.getProperty("test.test4")
        ).containsExactly("abc3", "abc4");

  }


}
