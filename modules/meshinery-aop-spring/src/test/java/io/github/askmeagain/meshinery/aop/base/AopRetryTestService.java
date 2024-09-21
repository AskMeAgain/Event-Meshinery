package io.github.askmeagain.meshinery.aop.base;

import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.aop.common.RetryType;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AopRetryTestService {

  private final AtomicInteger flag = new AtomicInteger();

  @MeshineryTaskBridge(write = "retryEnd", retryCount = 6, inMemoryRetry = RetryType.EVENT)
  public TestContext retryInEvent(TestContext context) {
    log.info("starting with retry now: {}", context.getId());
    if (flag.incrementAndGet() < 3) {
      log.error("error in between");
      throw new RuntimeException("err");
    }
    return context.toBuilder()
        .index(context.getIndex() + 1)
        .build();
  }

  @MeshineryTaskBridge(write = "retryEnd", retryCount = 6, inMemoryRetry = RetryType.MEMORY)
  public TestContext retryInMemory(TestContext context) {
    log.info("starting with retry now: {}", context.getId());
    if (flag.incrementAndGet() < 3) {
      log.error("error in between");
      throw new RuntimeException("err");
    }
    return context.toBuilder()
        .index(context.getIndex() + 1)
        .build();
  }

  @MeshineryTaskBridge(write = "test-result")
  public TestContext retryEnd(TestContext context) {
    log.info("It worked!!! {}", context.getId());
    return context;
  }
}
