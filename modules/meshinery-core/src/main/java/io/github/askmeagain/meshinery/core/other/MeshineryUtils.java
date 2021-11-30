package io.github.askmeagain.meshinery.core.other;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.task.TaskData;
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
  public static <I extends DataContext, O extends DataContext> CompletableFuture<O> combineProcessors(
      List<MeshineryProcessor<DataContext, DataContext>> processorList,
      I context,
      Executor executor,
      Map<String, String> mdc,
      TaskData taskData
  ) {
    CompletableFuture<DataContext> temp = CompletableFuture.completedFuture(context);

    for (MeshineryProcessor<DataContext, DataContext> newProcessor : processorList) {
      temp = temp.thenCompose(x -> {
        MDC.setContextMap(mdc);
        TaskData.setTaskData(taskData);
        return newProcessor.processAsync(x, executor);
      });
    }

    return (CompletableFuture<O>) temp;
  }

  public static <I extends DataContext, O extends DataContext> MeshineryProcessor<I, O> applyDecorators(
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
