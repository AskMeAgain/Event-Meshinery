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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
@Slf4j
@RequiredArgsConstructor
public class MeshineryTask<K, C extends DataContext> {

  private final long backoffTimeMilli;
  @Getter private final K inputKey;
  @Getter private final String taskName;
  @Getter private final TaskData taskData;
  @Getter private final MeshineryConnector<K, C> inputConnector;
  @Getter private final MeshineryConnector<K, C> outputConnector;
  @Getter private final DataInjectingExecutorService executorService;
  @Getter private final Function<Throwable, DataContext> handleException;
  @Getter private final List<MeshineryProcessor<DataContext, DataContext>> processorList;
  Instant nextExecution = Instant.now();

  public ConnectorKey getConnectorKey() {
    return ConnectorKey.builder()
        .connector((MeshineryConnector<Object, DataContext>) inputConnector)
        .key(inputKey)
        .build();
  }

  /**
   * Pulls the next batch of data from the input source. Keeps the backoff period in mind, which in this case returns
   * empty list and doesnt poll the source
   *
   * @return returns Taskruns
   */
  public List<TaskRun> getNewTaskRuns() {
    var now = Instant.now();

    if (!now.isAfter(nextExecution)) {
      return Collections.emptyList();
    }

    log.debug("Creating new TaskRuns");

    nextExecution = now.plusMillis(backoffTimeMilli);
    return inputConnector.getInputs(inputKey)
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
  }
}
