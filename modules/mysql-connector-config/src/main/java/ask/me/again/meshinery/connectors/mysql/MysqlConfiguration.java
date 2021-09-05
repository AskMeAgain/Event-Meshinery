package ask.me.again.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class MysqlConfiguration {

  @Bean
  public Jdbi jdbi(MysqlProperties mysqlProperties) {
    return Jdbi.create(mysqlProperties.getConnectionString(), mysqlProperties.getUser(), mysqlProperties.getPassword());
  }

  @Bean
  @ConfigurationProperties(prefix = "meshinery.mysql")
  public MysqlProperties mysqlProperties() {
    return new MysqlProperties();
  }
}
