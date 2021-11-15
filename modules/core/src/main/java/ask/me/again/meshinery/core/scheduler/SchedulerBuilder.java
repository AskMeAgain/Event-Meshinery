package ask.me.again.meshinery.core.scheduler;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.task.TaskRun;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import lombok.SneakyThrows;

public class SchedulerBuilder {

  Runnable shutdownHook;
  int backpressureLimit = 200;
  boolean isBatchJob;
  List<MeshineryTask<? extends Object, ? extends Context>> tasks = new ArrayList<>();
  List<ExecutorService> executorServices = new ArrayList<>();
  ConcurrentLinkedQueue<TaskRun> todoQueue = new ConcurrentLinkedQueue<>();

  public SchedulerBuilder task(MeshineryTask<?, ? extends Context> task) {
    tasks.add(task);
    return this;
  }

  public SchedulerBuilder backpressureLimit(int backpressureLimit) {
    this.backpressureLimit = backpressureLimit;
    return this;
  }

  public SchedulerBuilder tasks(List<MeshineryTask<?, ? extends Context>> task) {
    tasks.addAll(task);
    return this;
  }

  public SchedulerBuilder registerShutdownHook(Runnable shutdownHook) {
    this.shutdownHook = shutdownHook;
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
        shutdownHook
    );
  }
}