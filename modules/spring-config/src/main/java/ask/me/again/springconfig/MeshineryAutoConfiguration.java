package ask.me.again.springconfig;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTask;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
@ConditionalOnMissingBean(MeshineryAutoConfiguration.class)
public class MeshineryAutoConfiguration {

  @Value("${meshinery.batch-job:false}")
  private boolean isBatchJob;

  @Bean
  @ConditionalOnMissingBean(CustomizeShutdownHook.class)
  public CustomizeShutdownHook shutdownhook() {
    return () -> {
      //empty
    };
  }

  @Bean
  @ConditionalOnMissingBean(CustomizeStartupHook.class)
  public CustomizeStartupHook startupHook() {
    return scheduler -> {
      //empty
    };
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler roundRobinScheduler(
      List<MeshineryTask<?, ?>> tasks, CustomizeShutdownHook shutdownHook, CustomizeStartupHook startupHook
  ) {
    return RoundRobinScheduler.builder()
        .isBatchJob(isBatchJob)
        .registerShutdownHook(shutdownHook::run)
        .registerStartupHook(startupHook::apply)
        .tasks(tasks)
        .buildAndStart();
  }

}
