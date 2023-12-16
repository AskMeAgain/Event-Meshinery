package io.github.askmeagain.meshinery.connectors.pubsub.e2e;

import io.github.askmeagain.meshinery.connectors.pubsub.EnableMeshineryPubSub;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EnableMeshineryPubSub(context = TestContext.class)
public class E2ePubSubTestConfiguration {

}

