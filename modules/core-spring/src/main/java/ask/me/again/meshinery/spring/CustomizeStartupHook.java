package ask.me.again.meshinery.spring;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.function.Consumer;

@SuppressWarnings("checkstyle:MissingJavadocType")
@FunctionalInterface
public interface CustomizeStartupHook extends Consumer<RoundRobinScheduler> {
}
