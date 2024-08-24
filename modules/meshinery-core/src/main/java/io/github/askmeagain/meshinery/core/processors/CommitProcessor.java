package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitProcessor<C extends MeshineryDataContext>
    implements MeshineryProcessor<C, C> {

  private final Supplier<MeshineryInputSource<?, C>> meshineryInputSource;

  @Override
  public C processAsync(C context) {
    return meshineryInputSource.get().commit(context);
  }
}
