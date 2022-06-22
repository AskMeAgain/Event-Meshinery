package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
  public DynamicMysqlConnectorRegistration dynamicMysqlConnectorRegistration(
      ApplicationContext applicationContext,
      ObjectProvider<ObjectMapper> objectMapper,
      ObjectProvider<MeshineryMysqlProperties> meshineryMysqlProperties
  ) {
    return new DynamicMysqlConnectorRegistration(applicationContext, objectMapper, meshineryMysqlProperties);
  }

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @Validated
  @ConfigurationProperties("meshinery.connectors.mysql")
  public MeshineryMysqlProperties mysqlProperties() {
    return new MeshineryMysqlProperties();
  }
}
