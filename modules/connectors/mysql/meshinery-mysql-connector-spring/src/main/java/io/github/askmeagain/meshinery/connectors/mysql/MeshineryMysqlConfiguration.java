package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MeshineryMysqlConfiguration {

  @Bean
  public static DynamicMysqlConnectorRegistration dynamicMysqlConnectorRegistration(
      ApplicationContext applicationContext,
      ObjectProvider<ObjectMapper> objectMapper,
      ObjectProvider<MeshineryMysqlProperties> meshineryMysqlProperties
  ) {
    return new DynamicMysqlConnectorRegistration(applicationContext, objectMapper, meshineryMysqlProperties);
  }

  @Bean
  @Validated
  @ConfigurationProperties("meshinery.connectors.mysql")
  public MeshineryMysqlProperties mysqlProperties(@Autowired(required = false) DataSourceProperties dataSource) {
    var properties = new MeshineryMysqlProperties();

    if (dataSource != null) {
      properties.setConnectionString(dataSource.getUrl());
      properties.setPassword(dataSource.getPassword());
      properties.setUser(dataSource.getUsername());
      properties.setSchema(dataSource.getName());
    }

    return properties;
  }
}
