package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;


@Slf4j
public class OtelProcessorDecorator<I extends MeshineryDataContext, O extends MeshineryDataContext>
    implements ProcessorDecorator<I, O> {

  private final Tracer tracer;

  public OtelProcessorDecorator(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("OtelProcessorDecorator");
  }

  public MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor) {
    var processorName = getTaskData().getSingle(TASK_NAME);

    return context -> {
      var parentContext = OtelContextManager.span.get().getSpanContext();

      var span = tracer.spanBuilder(processor.getClass().getName())
          .setAttribute("task_name", processorName)
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
