package io.github.askmeagain.meshinery.core.shutdown;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Order(2)
@RequiredArgsConstructor
@RestController
@ConditionalOnProperty(value = "meshinery.core.shutdown-api", havingValue = "true")
public class ShutdownApiController {

  @Autowired
  private final RoundRobinScheduler roundRobinScheduler;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @PostMapping("/shutdown")
  public void injectContext() {
    roundRobinScheduler.gracefulShutdown();
  }
}
