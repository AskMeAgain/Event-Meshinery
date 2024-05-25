package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;


/**
 * A decorator for a processor. This will be wrapped around each processor
 *
 * @param <I> input type
 * @param <O> output type
 */
@FunctionalInterface
public interface ProcessorDecorator<I extends MeshineryDataContext, O extends MeshineryDataContext> {
  MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
