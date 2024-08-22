package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitProcessor<C extends MeshineryDataContext>
    implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {

  private final Supplier<MeshineryInputSource<?, C>> meshineryInputSource;

  @Override
  public MeshineryDataContext processAsync(MeshineryDataContext context) {
    return meshineryInputSource.get().commit((C) context);
  }
}
