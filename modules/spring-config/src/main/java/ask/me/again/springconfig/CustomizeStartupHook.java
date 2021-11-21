package ask.me.again.springconfig;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;

@FunctionalInterface
public interface CustomizeStartupHook {
  void apply(RoundRobinScheduler roundRobinScheduler);
}
