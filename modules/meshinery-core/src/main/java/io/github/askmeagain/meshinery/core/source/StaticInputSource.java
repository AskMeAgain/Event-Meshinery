package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class StaticInputSource<K, C extends MeshineryDataContext> implements MeshineryInputSource<K, C> {

  @Getter
  private final String name;
  private final Function<List<K>, List<C>> staticInput;

  @Override
  public List<C> getInputs(List<K> keys) {
    return staticInput.apply(keys);
  }

  @Override
  public C commit(C context) {
    return context;
  }
}
