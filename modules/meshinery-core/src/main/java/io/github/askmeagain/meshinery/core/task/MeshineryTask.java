package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.scheduler.ConnectorKey;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
@Slf4j
public class MeshineryTask<K, C extends DataContext> {

  private final long backoffTimeMilli;
  @Getter private final List<K> inputKeys;
  @Getter private final String taskName;
  @Getter private TaskData taskData;
  @Getter private final MeshineryConnector<K, C> inputConnector;
  @Getter private final MeshineryConnector<K, C> outputConnector;
  @Getter private final DataInjectingExecutorService executorService;
  @Getter private final Function<Throwable, DataContext> handleException;
  @Getter private final List<MeshineryProcessor<DataContext, DataContext>> processorList;
  Instant nextExecution = Instant.now();

  public MeshineryTask(
      long backoffTimeMilli,
      List<K> inputKeys,
      String taskName,
      TaskData taskData,
      MeshineryConnector<K, C> inputConnector,
      MeshineryConnector<K, C> outputConnector,
      DataInjectingExecutorService executorService,
      Function<Throwable, DataContext> handleException,
      List<MeshineryProcessor<DataContext, DataContext>> processorList
  ) {
    if (inputConnector != null) {
      taskData = inputConnector.addToTaskData(taskData);
    }

    this.backoffTimeMilli = backoffTimeMilli;
    this.inputKeys = inputKeys;
    this.taskName = taskName;
    this.taskData = taskData;

    this.inputConnector = inputConnector;
    this.outputConnector = outputConnector;
    this.executorService = executorService;
    this.handleException = handleException;
    this.processorList = processorList;
  }

  public ConnectorKey getConnectorKey() {
    return ConnectorKey.builder()
        .connector((MeshineryConnector<Object, DataContext>) inputConnector)
        .key(inputKeys)
        .build();
  }

  public MeshineryTask<K, C> withTaskData(TaskData taskData) {
    this.taskData = taskData;
    return this;
  }

  public void verifyTask() {
    Objects.requireNonNull(inputConnector, "Input source not specified");

    if (inputKeys.isEmpty() && !taskData.has(TASK_IGNORE_NO_KEYS_WARNING)) {
      throw new RuntimeException("Input Keys not defined for task %s. ".formatted(taskName) +
          "If this is intended add %s property to task to ignore this".formatted(TASK_IGNORE_NO_KEYS_WARNING));
    }
  }

  /**
   * Pulls the next batch of data from the input source. Keeps the backoff period in mind, which in this case returns
   * empty list and doesnt poll the source
   *
   * @return returns TaskRuns
   */
  public List<TaskRun> getNewTaskRuns() {
    var now = Instant.now();

    if (!now.isAfter(nextExecution)) {
      return Collections.emptyList();
    }

    try {
      TaskData.setTaskData(taskData);
      nextExecution = now.plusMillis(backoffTimeMilli);
      return inputConnector.getInputs(inputKeys)
          .stream()
          .map(input -> TaskRun.builder()
              .taskName(getTaskName())
              .taskData(taskData)
              .id(input.getId())
              .future(CompletableFuture.completedFuture(input))
              .executorService(getExecutorService())
              .queue(new LinkedList<>(getProcessorList()))
              .handleError(getHandleException())
              .build())
          .toList();
    } finally {
      TaskData.clearTaskData();
    }
  }
}
