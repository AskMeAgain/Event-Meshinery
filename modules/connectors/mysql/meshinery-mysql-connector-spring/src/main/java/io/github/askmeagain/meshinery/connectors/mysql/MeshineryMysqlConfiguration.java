package io.github.askmeagain.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@EnableConfigurationProperties
public class MeshineryMysqlConfiguration {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @Bean
  public Jdbi jdbi(MysqlProperties mysqlProperties) {
    return Jdbi.create(
        mysqlProperties.getConnectionString(),
        mysqlProperties.getUser(),
        mysqlProperties.getPassword()
    ).installPlugin(new Jackson2Plugin());
  }

  @Bean
  @ConfigurationProperties(prefix = "meshinery.connectors.mysql")
  public MysqlProperties mysqlProperties() {
    return new MysqlProperties();
  }
}
