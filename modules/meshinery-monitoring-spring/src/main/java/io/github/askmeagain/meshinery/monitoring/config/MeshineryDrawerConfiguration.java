package io.github.askmeagain.meshinery.monitoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.monitoring.MeshineryDrawProperties;
import io.github.askmeagain.meshinery.monitoring.MeshineryDrawer;
import io.github.askmeagain.meshinery.monitoring.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.NodeCustomizer;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@EnableConfigurationProperties
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryDrawerConfiguration {

  @Bean
  @Validated
  @ConfigurationProperties("meshinery.draw")
  public MeshineryDrawProperties drawerProperties() {
    return new MeshineryDrawProperties();
  }

  @Bean
  MeshineryDrawer setupMeshineryDrawer(List<MeshineryTask<?, ?>> tasks) {
    return new MeshineryDrawer(tasks);
  }

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @ConditionalOnMissingBean(NodeCustomizer.class)
  public NodeCustomizer nodeCustomizer() {
    return new NodeCustomizer() {
    };
  }

  @Bean
  @ConditionalOnMissingBean(EdgeCustomizer.class)
  public EdgeCustomizer edgeCustomizer() {
    return new EdgeCustomizer() {
    };
  }

  @Bean
  @ConditionalOnMissingBean(GraphCustomizer.class)
  public GraphCustomizer graphCustomizer() {
    return new GraphCustomizer() {
    };
  }
}
