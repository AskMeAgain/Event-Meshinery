package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;

@SuppressWarnings("checkstyle:MissingJavadocType")
@FunctionalInterface
public interface ProcessorDecorator<I extends DataContext, O extends DataContext> {
  MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
