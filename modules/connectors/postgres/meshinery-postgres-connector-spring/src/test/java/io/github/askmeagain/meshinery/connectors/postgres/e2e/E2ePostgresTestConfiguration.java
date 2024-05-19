package io.github.askmeagain.meshinery.connectors.postgres.e2e;

import io.github.askmeagain.meshinery.connectors.mysql.EnableMeshineryPostgres;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EnableMeshineryPostgres(context = TestContext.class)
public class E2ePostgresTestConfiguration {
}
