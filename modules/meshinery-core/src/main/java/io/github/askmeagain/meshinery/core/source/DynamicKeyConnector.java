package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
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
public class DynamicKeyConnector<K, C extends MeshineryDataContext> implements MeshinerySourceConnector<K, C> {

  @Getter
  String name;
  Function<C, List<K>> keySupplier;
  MeshineryInputSource<K, C> outerInputSource;
  MeshineryInputSource<K, C> innerInputSource;
  MeshineryOutputSource<K, C> innerOutputSource;

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

  @Override
  public C commit(C context) {
    return context;
  }
}
