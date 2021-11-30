package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.function.Consumer;

@SuppressWarnings("checkstyle:MissingJavadocType")
@FunctionalInterface
public interface CustomizeStartupHook extends Consumer<RoundRobinScheduler> {
}
