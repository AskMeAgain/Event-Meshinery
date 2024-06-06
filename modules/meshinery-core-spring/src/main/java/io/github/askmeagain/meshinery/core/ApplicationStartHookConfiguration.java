package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "meshinery.core", value = "start-immediately", havingValue = "true",
    matchIfMissing = true)
public class ApplicationStartHookConfiguration {

  private final RoundRobinScheduler roundRobinScheduler;

  @Order
  @EventListener
  public void startScheduler(ApplicationReadyEvent e) {
    roundRobinScheduler.start();
  }
}