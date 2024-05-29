package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.scheduler.ConnectorKey;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeshineryTask<K, C extends MeshineryDataContext> {

  private final long backoffTimeMilli;
  @Getter private final List<K> inputKeys;
  @Getter private final String taskName;
  @Getter private TaskData taskData;
  @With(AccessLevel.PRIVATE)
  @Getter private final MeshineryInputSource<K, C> inputConnector;

  @Getter private final MeshineryOutputSource<K, C> outputConnector;
  @Getter private final Function<Throwable, MeshineryDataContext> handleException;
  @Getter private final List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> processorList;
  Instant nextExecution = Instant.now();

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask(
      long backoffTimeMilli,
      List<K> inputKeys,
      String taskName,
      TaskData taskData,
      MeshineryInputSource<K, C> inputConnector,
      MeshineryOutputSource<K, C> outputConnector,
      Function<Throwable, MeshineryDataContext> handleException,
      List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> processorList
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
    this.handleException = handleException;
    this.processorList = processorList;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public ConnectorKey getConnectorKey() {
    return ConnectorKey.builder()
        .connector((MeshineryInputSource<Object, MeshineryDataContext>) inputConnector)
        .key(inputKeys)
        .build();
  }

  public MeshineryTask<K, C> withTaskData(TaskData taskData) {
    this.taskData = taskData;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void verifyTask() {
    Objects.requireNonNull(inputConnector, "Input source not specified");

    if (inputKeys.isEmpty() && !taskData.has(TASK_IGNORE_NO_KEYS_WARNING)) {
      throw new RuntimeException("Input Keys not defined for task %s. ".formatted(taskName)
          + "If this is intended add %s property to task to ignore this".formatted(TASK_IGNORE_NO_KEYS_WARNING));
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
              .queue(new LinkedList<>(getProcessorList()))
              .handleError(getHandleException())
              .build())
          .toList();
    } finally {
      TaskData.clearTaskData();
    }
  }

  public MeshineryTask<K, C> withNewInputConnector(MeshineryInputSource<?, ?> decoratedInput) {
    return this.withInputConnector((MeshineryInputSource<K, C>) decoratedInput);
  }
}
