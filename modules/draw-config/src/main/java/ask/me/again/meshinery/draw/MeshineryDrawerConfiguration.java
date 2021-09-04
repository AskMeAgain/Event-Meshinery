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
  MeshineryDrawer setupMeshineryDrawer(ApplyNode applyNode, ApplyEdge applyEdge, List<MeshineryTask<?, ?>> tasks) {
    return MeshineryDrawer.builder()
      .tasks(tasks)
      .edgeAssignment(applyEdge)
      .nodeAssignment(applyNode)
      .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public ApplyNode applyNode() {
    return new ApplyNode() {
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public ApplyEdge applyEdge() {
    return new ApplyEdge() {
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public ApplyGraph applyGraph() {
    return new ApplyGraph() {
    };
  }
}
