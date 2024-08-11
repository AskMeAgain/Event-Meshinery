package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.DefaultPubSubNameResolver;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PubSubInputTest extends AbstractPubSubTestBase {

  @Test
  void testInputOutput() throws Exception {
    //Arrange --------------------------------------------------------------------------------
    var pubSubProperties = new MeshineryPubSubProperties();
    pubSubProperties.setAckImmediatly(true);
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
        credentialProvider,
        new DefaultPubSubNameResolver()
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

    createTopic(TOPIC + "_abc");
    createSubscription(TOPIC + "_abc", TOPIC + "_abc_subscription");

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(TOPIC + "_abc", value1, new TaskData());
    output.writeOutput(TOPIC + "_abc", value2, new TaskData());

    var result1 = input.getInputs(List.of(TOPIC + "_abc"));
    var result2 = input.getInputs(List.of(TOPIC + "_abc"));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .allMatch(x -> x.getMetadata(MeshineryPubSubProperties.PUBSUB_ACK_METADATA_FIELD_NAME) != null)
        .allMatch(x -> x.getId().equals("1"));
    assertThat(result2)
        .hasSize(1)
        .allMatch(x -> x.getMetadata(MeshineryPubSubProperties.PUBSUB_ACK_METADATA_FIELD_NAME) != null)
        .allMatch(x -> x.getId().equals("2"));

    output.close();
  }
}
