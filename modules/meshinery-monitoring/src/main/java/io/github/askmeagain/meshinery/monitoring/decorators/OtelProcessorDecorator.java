package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;


@Slf4j
@RequiredArgsConstructor
public class OtelProcessorDecorator<I extends MeshineryDataContext, O extends MeshineryDataContext>
    implements ProcessorDecorator<I, O> {


  public MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor) {
    var taskName = getTaskData().getSingle(TASK_NAME);
    //var processorName = MeshineryMonitoringUtils.convertLambdaProcessorName(processor.getClass());

    return context -> {
      SpanContext parentContext = OtelContextManager.span.get().getSpanContext();

      var span = OtelContextManager.tracer.get().spanBuilder(taskName)
          .setAttribute("task_name", taskName)
          .setAttribute("step", processor.getClass().getSimpleName())
          .setParent(Context.current().with(Span.wrap(parentContext)))
          .startSpan();

      try {
        return processor.processAsync(context);
      } catch (Exception e) {
        span.recordException(e);
        throw e;
      } finally {
        span.end();
      }
    };
  }
}
