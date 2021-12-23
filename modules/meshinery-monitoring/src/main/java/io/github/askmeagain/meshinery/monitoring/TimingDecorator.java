package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;

/**
 * Adds a timing metric to a prometheus registry. Uses TASK_NAME of the taskData properties to label the metric
 *
 * @param <I> Input Context Type
 * @param <O> Output Context Type
 */
@Slf4j
public class TimingDecorator<I extends DataContext, O extends DataContext> implements ProcessorDecorator<I, O> {

  /**
   * Wraps a processor and adds a monitoring around it.
   *
   * @param processor The processor to wrap
   * @return the new wrapped/decorated processor
   */
  public MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor) {
    var taskName = getTaskData().getSingle(TASK_NAME);
    var processorName = MeshineryMonitoringUtils.convertLambdaProcessorName(processor.getClass());

    var summary = MeshineryMonitoringService.REQUEST_TIME_SUMMARY.labels(taskName, processorName);
    var inProcessingCounter = MeshineryMonitoringService.IN_PROCESSING_COUNTER.labels(taskName);

    return (context, executor) -> {
      inProcessingCounter.inc();
      var begin = Instant.now();
      return processor.processAsync(context, executor).whenComplete((c1, exception) -> {
        var diff = Duration.between(begin, Instant.now());
        inProcessingCounter.dec();
        summary.observe(diff.toMillis());
      });
    };
  }
}
