package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@EnableConfigurationProperties
public class MeshineryMysqlConfiguration {

  @Bean
  public DynamicMysqlConnectorRegistration dynamicKafkaConnectorRegistration(
      ApplicationContext applicationContext,
      ObjectProvider<ObjectMapper> objectMapper,
      ObjectProvider<Jdbi> jdbi,
      ObjectProvider<MeshineryMysqlProperties> meshineryMysqlProperties
  ) {
    return new DynamicMysqlConnectorRegistration(applicationContext, objectMapper, meshineryMysqlProperties, jdbi);
  }

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

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
