package io.github.askmeagain.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@EnableConfigurationProperties
public class MeshineryMysqlConfiguration {

  @Bean
  @ConditionalOnMissingBean(Jdbi.class)
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Jdbi jdbi(MeshineryMysqlProperties mysqlProperties) {
    return Jdbi.create(
        mysqlProperties.getConnectionString(),
        mysqlProperties.getUser(),
        mysqlProperties.getPassword()
    ).installPlugin(new Jackson2Plugin());
  }

  @Bean
  @ConfigurationProperties(prefix = "meshinery.connectors.mysql")
  public MeshineryMysqlProperties mysqlProperties() {
    return new MeshineryMysqlProperties();
  }
}
