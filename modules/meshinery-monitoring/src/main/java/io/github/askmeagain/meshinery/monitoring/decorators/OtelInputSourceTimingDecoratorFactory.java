package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorTimingDecorator implements MeshineryInputSource<Object, MeshineryDataContext> {

    private final Tracer tracer;
    private final MeshineryInputSource<Object, MeshineryDataContext> innerConnector;
    @Getter(lazy = true)
    private final String name = innerConnector.getName();

    @Override
    public MeshineryDataContext commit(MeshineryDataContext context) {
      return context;
    }

    //TODO
    @Override
    public List<MeshineryDataContext> getInputs(List<Object> keys) {
      //      var span = tracer.spanBuilder("getInputs").startSpan();
      return innerConnector.getInputs(keys);

      //      try (var ignored = span.makeCurrent()) {
      //      } catch (Throwable t) {
      //        span.recordException(t);
      //        throw t;
      //      } finally {
      //        span.end();
      //      }
    }
  }
}
