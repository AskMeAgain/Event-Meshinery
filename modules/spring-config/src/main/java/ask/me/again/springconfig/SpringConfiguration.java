package ask.me.again.springconfig;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.service.MeshineryWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@RequiredArgsConstructor
public class SpringConfiguration {

  private final List<MeshineryTask<?, ?>> tasks;

  private final InputSource<?, ?> inputSource;

  private final AtomicBoolean atomicBoolean;

  @PostConstruct
  public void setup() {
    new MeshineryWorker(tasks, inputSource).start(atomicBoolean);
  }

}
