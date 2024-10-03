package io.github.askmeagain.meshinery.core.hooks;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.function.Consumer;

/**
 * Interface to implement a hook into the startup of the scheduler
 */
@FunctionalInterface
public interface CustomizeStartupHook extends Consumer<RoundRobinScheduler> {
}
