package io.github.askmeagain.meshinery.aop.e2e.base;

import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class E2eTestService {

  private final Map<String, AtomicInteger> map = new ConcurrentHashMap<>();

  @MeshineryTaskBridge
  public void executeViaJob(TestContext context) {
    log.info("executed inside job? " + context.getId());
  }

  @MeshineryTaskBridge(retryCount = 3)
  public TestContext retry3TimesTest(TestContext context) {
    map.computeIfAbsent(context.getId(), k -> new AtomicInteger(0));
    var result = map.get(context.getId()).incrementAndGet();

    if (result <= 3) {
      throw new RuntimeException("please retry");
    }

    log.info("received task3: {}", context.getId());
    return context;
  }
}
