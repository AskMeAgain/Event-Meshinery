package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class BranchProcessor<C extends DataContext> implements MeshineryProcessor<C, C> {

  private final List<MeshineryProcessor<C, C>> processorList;
  private final List<Predicate<C>> predicateList;

  public static <C extends DataContext> BranchProcessor<C> builder() {
    return new BranchProcessor<>(new ArrayList<>(), new ArrayList<>());
  }

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {

    for (int i = 0; i < processorList.size(); i++) {
      if (predicateList.get(i).test(context)) {
        return processorList.get(i).processAsync(context, executor);
      }
    }

    throw new RuntimeException("This branching processor didnt find any results");
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public BranchProcessor<C> branch(MeshineryProcessor<C, C> processor, Predicate<C> predicate) {
    processorList.add(processor);
    predicateList.add(predicate);
    return new BranchProcessor<>(processorList, predicateList);
  }
}
