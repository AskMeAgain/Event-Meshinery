package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;


@Slf4j
@RequiredArgsConstructor
public class OtelProcessorDecorator<I extends MeshineryDataContext, O extends MeshineryDataContext>
    implements ProcessorDecorator<I, O> {

  private final OpenTelemetry openTelemetry;

  private final Map<String, Tracer> tracerMap = new ConcurrentHashMap<>();

  public MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor) {
    var taskName = getTaskData().getSingle(TASK_NAME);
    var processorName = MeshineryMonitoringUtils.convertLambdaProcessorName(processor.getClass());

    var tracer = tracerMap.computeIfAbsent(processorName, openTelemetry::getTracer);

    return context -> {
      var otelContext = SpanContext.create(
          TraceId.fromBytes(context.getId().getBytes()),
          SpanId.fromBytes(processorName.getBytes()),
          TraceFlags.getDefault(),
          TraceState.getDefault()
      );

      var wrap = Span.wrap(otelContext);

      var with = Context.current().with(wrap);
      var span = tracer.spanBuilder(taskName).setParent(with).startSpan();

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
