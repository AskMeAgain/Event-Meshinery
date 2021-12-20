package io.github.askmeagain.meshinery.connectors.mysql.e2e;

import io.github.askmeagain.meshinery.connectors.mysql.EnableMeshineryMysql;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EnableMeshineryMysql(context = TestContext.class)
public class E2eMysqlTestConfiguration {
}
