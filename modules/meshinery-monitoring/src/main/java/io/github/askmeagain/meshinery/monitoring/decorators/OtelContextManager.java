package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OtelContextManager {

  public final Tracer tracer;
  public static final ThreadLocal<Span> span = new ThreadLocal<>();

  public OtelContextManager(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("OtelContextManager");
  }

  public void setup(MeshineryDataContext context) {
    span.set(tracer.spanBuilder(TaskData.getTaskData().getSingle(TaskDataProperties.TASK_NAME))
        .setAttribute("context-id", context.getId())
        .startSpan());
  }

  public void cleanup(MeshineryDataContext meshineryDataContext) {
    span.get().end();
    span.remove();
  }
}
