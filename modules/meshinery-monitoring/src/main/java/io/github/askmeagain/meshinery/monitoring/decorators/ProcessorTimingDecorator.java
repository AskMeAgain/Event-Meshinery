package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringUtils;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;

/**
 * Adds a timing metric to a prometheus registry. Uses TASK_NAME of the taskData properties to label the metric
 *
 * @param <C> Context Type
 */
@Slf4j
public class ProcessorTimingDecorator<C extends MeshineryDataContext> implements ProcessorDecorator<C> {

  /**
   * Wraps a processor and adds a monitoring around it.
   *
   * @param processor The processor to wrap
   * @return the new wrapped/decorated processor
   */
  public MeshineryProcessor<C, C> wrap(MeshineryProcessor<C, C> processor) {
    var taskName = getTaskData().getSingle(TASK_NAME);
    var processorName = MeshineryMonitoringUtils.convertLambdaProcessorName(processor.getClass());

    var summary = MeshineryMonitoringService.REQUEST_TIME_SUMMARY.labels(taskName, processorName);
    var inProcessingCounter = MeshineryMonitoringService.IN_PROCESSING_COUNTER.labels(taskName);

    return context -> {
      inProcessingCounter.inc();
      var begin = Instant.now();

      try {
        return processor.process(context);
      } finally {
        var diff = Duration.between(begin, Instant.now());
        inProcessingCounter.dec();
        summary.observe(diff.toMillis());
      }

    };
  }
}
