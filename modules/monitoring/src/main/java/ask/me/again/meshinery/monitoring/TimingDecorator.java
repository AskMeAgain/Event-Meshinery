package ask.me.again.meshinery.monitoring;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.ProcessorDecorator;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

import static ask.me.again.meshinery.core.task.TaskDataProperties.TASK_NAME;

@Slf4j
public class TimingDecorator<I extends Context, O extends Context> implements ProcessorDecorator<I, O> {

  public MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor) {
    var taskName = getTaskData().getSingle(TASK_NAME);
    var summary = MeshineryMonitoringService.requestTimeSummary.labels(taskName);

    return (context, executor) -> {
      var begin = Instant.now();
      return processor.processAsync(context, executor).whenComplete((c1, exception) -> {
        var diff = Duration.between(begin, Instant.now());
        summary.observe(diff.toMillis());
      });
    };
  }
}
