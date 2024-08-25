package io.github.askmeagain.meshinery.core.hooks;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.function.Consumer;

@SuppressWarnings("checkstyle:MissingJavadocType")
@FunctionalInterface
public interface CustomizeShutdownHook extends Consumer<RoundRobinScheduler> {
}