package io.github.askmeagain.meshinery.core.e2e;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.EnableMeshinery.KeyDataContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMeshinery(connector = @KeyDataContext(key = String.class, context = TestContext.class))
public class E2eMemoryTestConfiguration {
}
