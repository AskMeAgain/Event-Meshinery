package io.github.askmeagain.meshinery.connectors.pubsub;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class AbstractSpringPubSubTestBase extends AbstractPubSubTestBase {

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add(
        "meshinery.connectors.pubsub.emulatorEndpoint",
        PUB_SUB_EMULATOR_CONTAINER::getEmulatorEndpoint
    );
  }
}
