package ask.me.again.meshinery.connectors.mysql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
  classes = MysqlConfiguration.class,
  initializers = ConfigDataApplicationContextInitializer.class
)
class MysqlConfigurationTest {

  @Autowired
  MysqlProperties mysqlProperties;

  @Test
  void smokeTest() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    assertThat(mysqlProperties)
      .extracting(
        MysqlProperties::getConnectionString,
        MysqlProperties::getLimit,
        MysqlProperties::getPassword,
        MysqlProperties::getUser
      )
      .containsExactly(
        "jdbc:mysql://localhost:3306/db",
        1,
        "password",
        "user"
      );

  }
}
