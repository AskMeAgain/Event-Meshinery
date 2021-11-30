package io.github.askmeagain.meshinery.draw;

import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryDrawerConfiguration {

  @Bean
  @ConfigurationProperties("meshinery.draw")
  public DrawerProperties drawerProperties() {
    return new DrawerProperties();
  }

  @Bean
  MeshineryDrawer setupMeshineryDrawer(
      NodeCustomizer nodeCustomizer,
      EdgeCustomizer edgeCustomizer,
      List<MeshineryTask<?, ?>> tasks,
      GraphCustomizer graphCustomizer,
      DrawerProperties drawerProperties
  ) {
    return MeshineryDrawer.builder()
        .tasks(tasks)
        .properties(drawerProperties)
        .edgeAssignment(edgeCustomizer)
        .nodeAssignment(nodeCustomizer)
        .graphAssignment(graphCustomizer)
        .build();
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
