package io.github.askmeagain.meshinery.aop.e2e.base;

import io.github.askmeagain.meshinery.aop.common.MeshineryReadTask;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class E2eTestService {

  @MeshineryReadTask(event = "test", context = TestContext.class)
  public void executeViaJob(DataContext context) {
    log.info("executed inside job? " + context.getId());
  }
}
