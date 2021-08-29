package ask.me.again.springconfig;

import ask.me.again.core.common.ReactiveTask;
import ask.me.again.core.common.InputSource;
import ask.me.again.core.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@RequiredArgsConstructor
public class SpringConfiguration {

  private final List<ReactiveTask<?, ?>> tasks;

  private final InputSource<?, ?> inputSource;

  private final AtomicBoolean atomicBoolean;

  @PostConstruct
  public void setup() {
    new WorkerService(tasks, inputSource).start(atomicBoolean);
  }

}
