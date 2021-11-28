package ask.me.again.meshinery.core.other;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.ProcessorDecorator;
import ask.me.again.meshinery.core.task.TaskData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class MeshineryUtils {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static <I extends Context, O extends Context> CompletableFuture<O> combineProcessors(
      List<MeshineryProcessor<Context, Context>> processorList,
      I context,
      Executor executor,
      Map<String, String> mdc,
      TaskData taskData
  ) {
    CompletableFuture<Context> temp = CompletableFuture.completedFuture(context);

    for (MeshineryProcessor<Context, Context> newProcessor : processorList) {
      temp = temp.thenCompose(x -> {
        MDC.setContextMap(mdc);
        TaskData.setTaskData(taskData);
        return newProcessor.processAsync(x, executor);
      });
    }

    return (CompletableFuture<O>) temp;
  }

  public static <I extends Context, O extends Context> MeshineryProcessor<I, O> applyDecorators(
      MeshineryProcessor<I, O> nextProcessor,
      List<ProcessorDecorator<I, O>> processorDecorator
  ) {
    var innerProcessor = nextProcessor;

    for (var decorator : processorDecorator) {
      innerProcessor = decorator.wrap(innerProcessor);
    }

    return innerProcessor;
  }
}
