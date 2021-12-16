package io.github.askmeagain.meshinery.connectors.kafka;

import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@MockBean(KafkaProducerFactory.class)
@SpringJUnitConfig(MeshineryKafkaConfiguration.class)
@TestPropertySource(properties = {
    "meshinery.connectors.kafka.bootstrap-servers=abc",
    "meshinery.connectors.kafka.group-id=groupid",
    "meshinery.connectors.kafka.consumer-properties.test.test1=abc1",
    "meshinery.connectors.kafka.consumer-properties.test.test2=abc2",
    "meshinery.connectors.kafka.producer-properties.test.test3=abc3",
    "meshinery.connectors.kafka.producer-properties.test.test4=abc4",
})
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
