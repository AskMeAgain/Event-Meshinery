package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder
public class DynamicKeyConnector<K, C extends DataContext> implements MeshineryConnector<K, C> {

  @Getter
  String name;
  Function<C, List<K>> keySupplier;
  InputSource<K, C> outerInputSource;
  InputSource<K, C> innerInputSource;
  OutputSource<K, C> innerOutputSource;

  @Override
  public List<C> getInputs(List<K> key) {

    return outerInputSource.getInputs(key).stream()
        .map(outerCtx -> innerInputSource.getInputs(keySupplier.apply(outerCtx)))
        .flatMap(Collection::stream)
        .toList();
  }


  @Override
  public void writeOutput(K key, C output, TaskData taskData) {
    innerOutputSource.writeOutput(key, output, taskData);
  }
}
