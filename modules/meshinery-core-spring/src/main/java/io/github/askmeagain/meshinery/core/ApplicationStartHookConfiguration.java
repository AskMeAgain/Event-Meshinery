package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskVerifier;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
public class ApplicationStartHookConfiguration {

  private final RoundRobinScheduler roundRobinScheduler;
  private final List<MeshineryTask<?, ?>> tasks;

  @EventListener
  @Order(1000)
  public void startScheduler(ApplicationReadyEvent e) {
    roundRobinScheduler.start();
  }

  @Order(0)
  @EventListener
  public void verifyTasks(ApplicationReadyEvent e) {
    MeshineryTaskVerifier.verifyTasks(tasks);
  }
}
