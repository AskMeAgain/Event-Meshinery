package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.OutputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class OtelOutputSourceDecoratorFactory implements OutputSourceDecoratorFactory {

  private final OpenTelemetry openTelemetry;

  @Override
  public MeshineryOutputSource<?, MeshineryDataContext> decorate(
      MeshineryOutputSource<?, ? extends MeshineryDataContext> inputConnector
  ) {
    var tracer = openTelemetry.getTracer(inputConnector.getClass().getName());

    return new ConnectorOutputDecorator(
        tracer,
        (MeshineryOutputSource<Object, MeshineryDataContext>) inputConnector
    );
  }

  @Slf4j
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorOutputDecorator implements MeshineryOutputSource<Object, MeshineryDataContext> {

    private final Tracer tracer;
    private final MeshineryOutputSource<Object, MeshineryDataContext> innerConnector;
    @Getter(lazy = true)
    private final String name = innerConnector.getName();

    @Override
    public void writeOutput(Object key, MeshineryDataContext ctx, TaskData taskData) {
      var traceId = ctx.getMetadata("otel-trace-id");
      var spanId = ctx.getMetadata("otel-span-id");

      Span span;
      if (traceId == null) {
        span = tracer.spanBuilder(ctx.getId()).startSpan();
      } else {
        var remoteContext = SpanContext.createFromRemoteParent(
            traceId,
            spanId,
            TraceFlags.getSampled(),
            TraceState.getDefault()
        );
        span = tracer.spanBuilder(key.toString())
            .setParent(Context.current().with(Span.wrap(remoteContext)))
            .startSpan();
      }

      var finalCtx = ctx
          .setMetadata("otel-trace-id", span.getSpanContext().getTraceId())
          .setMetadata("otel-span-id", span.getSpanContext().getSpanId());

      innerConnector.writeOutput(key, finalCtx, taskData);

      span.end();
    }
  }
}
