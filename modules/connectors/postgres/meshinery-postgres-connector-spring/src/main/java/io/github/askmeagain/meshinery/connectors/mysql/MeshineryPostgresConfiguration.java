package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.postgres.MeshineryPostgresProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Validated
@Configuration
@EnableConfigurationProperties
public class MeshineryPostgresConfiguration {

  @Bean
  public static DynamicPostgresConnectorRegistration dynamicMysqlConnectorRegistration(
      ApplicationContext applicationContext,
      ObjectProvider<ObjectMapper> objectMapper,
      ObjectProvider<MeshineryPostgresProperties> meshineryPostgresProperties
  ) {
    return new DynamicPostgresConnectorRegistration(applicationContext, objectMapper, meshineryPostgresProperties);
  }

  /**
   * Enrich the postgres properties with spring internals if they exist
   */
  @Bean
  @Validated
  @ConfigurationProperties("meshinery.connectors.postgres")
  public MeshineryPostgresProperties postgresProperties(@Autowired(required = false) DataSourceProperties dataSource) {
    var properties = new MeshineryPostgresProperties();

    if (dataSource != null) {
      properties.setConnectionString(dataSource.getUrl());
      properties.setPassword(dataSource.getPassword());
      properties.setUser(dataSource.getUsername());
    }

    return properties;
  }
}
