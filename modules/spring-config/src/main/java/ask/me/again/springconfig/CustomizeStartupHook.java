package ask.me.again.springconfig;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.function.Consumer;

@FunctionalInterface
public interface CustomizeStartupHook extends Consumer<RoundRobinScheduler> {
}
