package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;


@Slf4j
public class OtelProcessorDecorator<C extends MeshineryDataContext> implements ProcessorDecorator<C> {

  private final Tracer tracer;

  public OtelProcessorDecorator(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("OtelProcessorDecorator");
  }

  public MeshineryProcessor<C, C> wrap(MeshineryProcessor<C, C> processor) {
    var processorName = getTaskData().getSingle(TASK_NAME);

    return context -> {
      if (context == null) {
        return null;
      }
      var remoteContext = SpanContext.createFromRemoteParent(
          context.getMetadata("otel-trace-id"),
          context.getMetadata("otel-span-id"),
          TraceFlags.getSampled(),
          TraceState.getDefault()
      );

      var span = tracer.spanBuilder("Processor: " + processor.getClass().getName())
          .setAttribute("task_name", processorName) //TODO resolve correct name here
          .setParent(Context.current().with(Span.wrap(remoteContext)))
          .startSpan();

      try {
        return processor.process(context);
      } catch (Exception e) {
        span.recordException(e);
        throw e;
      } finally {
        span.end();
      }
    };
  }
}
