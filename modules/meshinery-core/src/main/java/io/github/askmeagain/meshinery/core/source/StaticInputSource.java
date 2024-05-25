package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class StaticInputSource<K, C extends DataContext> implements InputSource<K, C> {

  @Getter
  private final String name;
  private final Function<List<K>, List<C>> staticInput;

  @Override
  public List<C> getInputs(List<K> keys) {
    return staticInput.apply(keys);
  }
}
