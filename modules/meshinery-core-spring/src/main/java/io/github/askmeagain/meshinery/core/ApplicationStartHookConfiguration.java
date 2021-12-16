package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
public class ApplicationStartHookConfiguration {

  private final RoundRobinScheduler roundRobinScheduler;

  @EventListener
  public void onApplicationEvent(ApplicationReadyEvent event) {
    roundRobinScheduler.start();
  }
}
