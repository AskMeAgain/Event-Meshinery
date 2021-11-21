package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.task.TaskData;

@FunctionalInterface
public interface ProcessorDecorator<I extends Context, O extends Context> {
  MeshineryProcessor<I, O> wrap(MeshineryProcessor<I, O> processor);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
