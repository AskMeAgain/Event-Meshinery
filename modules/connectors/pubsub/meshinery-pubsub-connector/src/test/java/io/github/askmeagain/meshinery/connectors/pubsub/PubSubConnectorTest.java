package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.DefaultPubSubNameResolver;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PubSubConnectorTest extends AbstractPubSubTestBase {

  @Test
  void testPubSubConnector() throws Exception {
    //Arrange --------------------------------------------------------------------------------
    var pubSubProperties = new MeshineryPubSubProperties();
    pubSubProperties.setLimit(1);
    pubSubProperties.setProjectId(getProjectId());

    var pubSubConnector = new PubSubConnector<>(
        TestContext.class,
        new ObjectMapper(),
        pubSubProperties,
        getTransportChannelProvider(),
        getCredentialProvider(), new DefaultPubSubNameResolver()
    );

    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    pubSubConnector.writeOutput(TOPIC, value1);
    pubSubConnector.writeOutput(TOPIC, value2);

    var result1 = pubSubConnector.getInputs(List.of(TOPIC));
    var result2 = pubSubConnector.getInputs(List.of(TOPIC));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .contains(value1);
    assertThat(result2)
        .hasSize(1)
        .contains(value2);

    pubSubConnector.close();
  }
}