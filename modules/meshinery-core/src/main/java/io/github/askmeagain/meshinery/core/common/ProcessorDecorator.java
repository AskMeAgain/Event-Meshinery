package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;


/**
 * A decorator for a processor. This will be wrapped around each processor
 *
 * @param <C> input/output type
 */
@FunctionalInterface
public interface ProcessorDecorator<C extends MeshineryDataContext> {
  MeshineryProcessor<C, C> wrap(MeshineryProcessor<C, C> processor);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
