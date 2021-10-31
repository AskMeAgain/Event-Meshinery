package ask.me.again.meshinery.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class ListProcessor<Input extends Context, Output extends Context> implements MeshineryProcessor<Input, Output> {

  List<MeshineryProcessor<Context, Context>> processorList;

  private ListProcessor() {
    processorList = new ArrayList<>();
  }

  private ListProcessor(List<MeshineryProcessor<Context, Context>> newProcessorList) {
    this.processorList = newProcessorList;
  }

  public static <Input extends Context> ListProcessor<Input, Input> builder() {
    return new ListProcessor<>();
  }

  public <N extends Context> ListProcessor<Input, N> process(MeshineryProcessor<Output, N> newProcessor) {
    processorList.add((MeshineryProcessor<Context, Context>) newProcessor);
    return new ListProcessor<>(processorList);
  }

  @Override
  public CompletableFuture<Output> processAsync(Input context, Executor executor) {
    CompletableFuture<Context> temp = CompletableFuture.completedFuture(context);

    for (MeshineryProcessor<Context, Context> newProcessor : processorList) {
      temp = temp.thenCompose(x -> newProcessor.processAsync(x, executor));
    }
    return (CompletableFuture<Output>) temp;
  }
}
