package ask.me.again.meshinery.core.task;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.GraphData;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MdcInjectingExecutorService;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
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
public class MeshineryTask<K, C extends Context> {

  private final long backoffTime;
  @Getter private final K inputKey;
  @Getter private final String taskName;
  @Getter private final GraphData<K> graphData;
  @Getter private final InputSource<K, C> inputSource;
  @Getter private final OutputSource<K, C> defaultOutputSource;
  @Getter private final MdcInjectingExecutorService executorService;
  @Getter private final Function<Throwable, Context> handleException;
  @Getter private final List<MeshineryProcessor<Context, Context>> processorList;
  Instant nextExecution = Instant.now();

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

    nextExecution = now.plusMillis(backoffTime);
    return inputSource.getInputs(inputKey)
        .stream()
        .map(input -> TaskRun.builder()
            .taskName(getTaskName())
            .id(input.getId())
            .future(CompletableFuture.completedFuture(input))
            .executorService(getExecutorService())
            .queue(new LinkedList<>(getProcessorList()))
            .handleError(getHandleException())
            .build())
        .toList();
  }
}
