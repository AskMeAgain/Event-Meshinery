package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.common.MeshineryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MeshineryDrawerConfiguration {

  @Bean
  MeshineryDrawer setupMeshineryDrawer(NodeCustomizer nodeCustomizer, EdgeCustomizer edgeCustomizer, List<MeshineryTask<?, ?>> tasks) {
    return MeshineryDrawer.builder()
        .tasks(tasks)
        .edgeAssignment(edgeCustomizer)
        .nodeAssignment(nodeCustomizer)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public NodeCustomizer applyNode() {
    return new NodeCustomizer() {
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public EdgeCustomizer applyEdge() {
    return new EdgeCustomizer() {
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public GraphCustomizer applyGraph() {
    return new GraphCustomizer() {
    };
  }
}
