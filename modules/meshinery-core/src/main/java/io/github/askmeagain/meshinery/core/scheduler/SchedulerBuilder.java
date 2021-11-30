package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskRun;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import lombok.SneakyThrows;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class SchedulerBuilder {

  List<? extends Consumer<RoundRobinScheduler>> shutdownHook = Collections.emptyList();
  List<ProcessorDecorator<DataContext, DataContext>> processorDecorator = Collections.emptyList();
  List<? extends Consumer<RoundRobinScheduler>> startupHook = Collections.emptyList();
  int backpressureLimit = 200;
  boolean isBatchJob;
  List<MeshineryTask<? extends Object, ? extends DataContext>> tasks = new ArrayList<>();
  List<ExecutorService> executorServices = new ArrayList<>();
  ConcurrentLinkedQueue<TaskRun> todoQueue = new ConcurrentLinkedQueue<>();
  boolean gracefulShutdownOnError = true;

  public SchedulerBuilder task(MeshineryTask<?, ? extends DataContext> task) {
    tasks.add(task);
    return this;
  }

  public SchedulerBuilder backpressureLimit(int backpressureLimit) {
    this.backpressureLimit = backpressureLimit;
    return this;
  }

  public SchedulerBuilder tasks(List<MeshineryTask<?, ? extends DataContext>> task) {
    tasks.addAll(task);
    return this;
  }

  public SchedulerBuilder registerDecorators(List<ProcessorDecorator<DataContext, DataContext>> processorDecorators) {
    this.processorDecorator = processorDecorators;
    return this;
  }

  public SchedulerBuilder registerShutdownHook(List<? extends Consumer<RoundRobinScheduler>> shutdownHook) {
    this.shutdownHook = shutdownHook;
    return this;
  }

  public SchedulerBuilder registerStartupHook(List<? extends Consumer<RoundRobinScheduler>> startupHook) {
    this.startupHook = startupHook;
    return this;
  }

  public SchedulerBuilder gracefulShutdownOnError(boolean gracefulShutdownOnError) {
    this.gracefulShutdownOnError = gracefulShutdownOnError;
    return this;
  }

  public SchedulerBuilder isBatchJob(boolean flag) {
    isBatchJob = flag;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler buildAndStart() {
    return build().start();
  }

  @SneakyThrows
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler build() {
    return new RoundRobinScheduler(
        tasks,
        executorServices,
        todoQueue,
        backpressureLimit,
        isBatchJob,
        shutdownHook,
        startupHook,
        processorDecorator,
        gracefulShutdownOnError
    );
  }
}