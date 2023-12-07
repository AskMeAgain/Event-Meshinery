package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.connectors.postgres.MeshineryPostgresProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
    classes = MeshineryPostgresConfiguration.class,
    initializers = ConfigDataApplicationContextInitializer.class
)
class PostgresConfigurationTest {

  @Autowired
  MeshineryPostgresProperties postgresProperties;

  @Test
  void smokeTest() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    assertThat(postgresProperties)
        .extracting(
            MeshineryPostgresProperties::getConnectionString,
            MeshineryPostgresProperties::getLimit,
            MeshineryPostgresProperties::getPassword,
            MeshineryPostgresProperties::getUser
        )
        .containsExactly(
            "jdbc:postgres://localhost:5432/db",
            1,
            "password",
            "user"
        );

  }
}
