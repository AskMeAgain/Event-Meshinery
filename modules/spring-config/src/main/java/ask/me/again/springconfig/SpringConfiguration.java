package ask.me.again.springconfig;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SpringConfiguration {

  private final List<MeshineryTask<?, ?>> tasks;

  @Value("${meshinery.batch-job:false}")
  private boolean isBatchJob;

  @PostConstruct
  public void setup() {
    new RoundRobinScheduler(isBatchJob, tasks).start();
  }

}
