package ask.me.again.meshinery.core.utils.processor;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.utils.context.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TaskDataTestProcessor implements MeshineryProcessor<TestContext, TestContext> {

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.completedFuture(context.toBuilder()
        .id(context.getIndex() + getTaskData().getSingle("test"))
        .index(Integer.parseInt(context.getIndex() + getTaskData().getSingle("test")))
        .build());
  }
}
