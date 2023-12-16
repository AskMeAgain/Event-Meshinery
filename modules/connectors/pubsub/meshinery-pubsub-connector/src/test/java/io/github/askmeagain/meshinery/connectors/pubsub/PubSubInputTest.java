package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PubSubInputTest extends AbstractPubSubTestBase {

  @Test
  void testInputOutput() throws Exception {
    //Arrange --------------------------------------------------------------------------------
    var pubSubProperties = new MeshineryPubSubProperties();
    pubSubProperties.setLimit(1);
    pubSubProperties.setProjectId(getProjectId());

    var objectMapper = new ObjectMapper();
    var credentialProvider = getCredentialProvider();
    var transportChannelProvider = getTransportChannelProvider();

    var input = new PubSubInputSource<>(
        "default",
        objectMapper,
        TestContext.class,
        pubSubProperties,
        transportChannelProvider,
        credentialProvider
    );
    var output = new PubSubOutputSource<>(
        "default",
        objectMapper,
        pubSubProperties,
        transportChannelProvider,
        credentialProvider
    );

    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(TOPIC, value1);
    output.writeOutput(TOPIC, value2);

    var result1 = input.getInputs(List.of(TOPIC));
    var result2 = input.getInputs(List.of(TOPIC));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .contains(value1);
    assertThat(result2)
        .hasSize(1)
        .contains(value2);

    output.close();
  }
}
