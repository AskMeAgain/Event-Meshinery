package io.github.askmeagain.meshinery.core.hooks;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.function.Consumer;

/**
 * Hook into the shutdown process of the roundrobing scheduler.
 */
@FunctionalInterface
public interface CustomizeShutdownHook extends Consumer<RoundRobinScheduler> {
}