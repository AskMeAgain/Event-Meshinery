package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;

@RequiredArgsConstructor
public class OtelContextManager {

  public static final ThreadLocal<Tracer> tracer = new ThreadLocal<>();
  public static final ThreadLocal<Span> span = new ThreadLocal<>();
  private final OpenTelemetry openTelemetry;

  public void setup(MeshineryDataContext meshineryDataContext) {
    tracer.set(openTelemetry.getTracer(meshineryDataContext.getId()));
    span.set(tracer.get().spanBuilder(meshineryDataContext.getId() + "-" + TaskData.getTaskData().getSingle(TASK_NAME))
        .startSpan());
  }

  public void cleanup(MeshineryDataContext meshineryDataContext) {
    span.get().end();
    span.remove();
    tracer.remove();
  }
}
