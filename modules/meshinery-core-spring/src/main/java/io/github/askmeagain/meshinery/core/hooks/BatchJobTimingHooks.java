package io.github.askmeagain.meshinery.core.hooks;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
public class BatchJobTimingHooks implements CustomizeStartupHook, CustomizeShutdownHook {

  private Instant begin;

  @Override
  public void accept(RoundRobinScheduler roundRobinScheduler) {
    if (begin == null) {
      begin = Instant.now();
      log.info("Starting Batch job at " + begin.toString());
    } else {
      log.info("Scheduler ran for {} seconds", Duration.between(begin, Instant.now()).getSeconds());
    }
  }
}
