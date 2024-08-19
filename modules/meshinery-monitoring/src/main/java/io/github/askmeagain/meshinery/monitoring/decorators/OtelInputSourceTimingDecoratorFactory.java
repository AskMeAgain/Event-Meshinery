package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class OtelInputSourceTimingDecoratorFactory implements InputSourceDecoratorFactory {

  private final OpenTelemetry openTelemetry;

  @Override
  public MeshineryInputSource<?, MeshineryDataContext> decorate(
      MeshineryInputSource<?, ? extends MeshineryDataContext> inputConnector
  ) {
    var tracer = openTelemetry.getTracer(inputConnector.getClass().getName());

    return new ConnectorTimingDecorator(
        tracer,
        (MeshineryInputSource<Object, MeshineryDataContext>) inputConnector
    );
  }

  @Slf4j
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorTimingDecorator implements MeshineryInputSource<Object, MeshineryDataContext> {

    private final Tracer tracer;
    private final MeshineryInputSource<Object, MeshineryDataContext> innerConnector;
    @Getter(lazy = true)
    private final String name = innerConnector.getName();

    private final Map<String, Span> map = new ConcurrentHashMap<>();

    @Override
    public List<MeshineryDataContext> getInputs(List<Object> keys) {
      var joinedKeys = keys.stream()
          .map(Object::toString)
          .collect(Collectors.joining("-"));

      return innerConnector.getInputs(keys)
          .stream()
          .map(ctx -> {
            //create new span because its new
            var traceId = ctx.getMetadata("otel-trace-id");
            var spanId = ctx.getMetadata("otel-span-id");

            if (traceId == null) {
              var span = tracer.spanBuilder(ctx.getId() + " " + joinedKeys).startSpan();
              //so we can end the span later
              map.put(span.getSpanContext().getSpanId(), span);

              var tmp1 = ctx.setMetadata("otel-trace-id", span.getSpanContext().getTraceId());
              return tmp1.setMetadata("otel-span-id", span.getSpanContext().getSpanId());
            } else {
              var remoteContext = SpanContext.createFromRemoteParent(
                  traceId,
                  spanId,
                  TraceFlags.getSampled(),
                  TraceState.getDefault()
              );
              var span = tracer.spanBuilder(joinedKeys)
                  .setParent(Context.current().with(Span.wrap(remoteContext)))
                  .startSpan();
              map.put(span.getSpanContext().getSpanId(), span);
              var tmp1 = ctx.setMetadata("otel-trace-id", span.getSpanContext().getTraceId());
              return tmp1.setMetadata("otel-span-id", span.getSpanContext().getSpanId());
            }
          })
          .toList();
    }

    @Override
    public MeshineryDataContext commit(MeshineryDataContext context) {
      var removedSpan = map.remove(context.getMetadata("otel-span-id"));
      if (removedSpan != null) {
        removedSpan.end();
      }
      return innerConnector.commit(context);
    }
  }
}
