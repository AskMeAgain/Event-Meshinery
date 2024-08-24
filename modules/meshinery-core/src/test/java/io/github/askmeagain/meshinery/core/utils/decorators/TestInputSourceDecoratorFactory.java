package io.github.askmeagain.meshinery.core.utils.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TestInputSourceDecoratorFactory<K, C extends MeshineryDataContext>
    implements InputSourceDecoratorFactory<K, C> {

  private final AtomicInteger atomicInteger;

  @Override
  public MeshinerySourceConnector<K, C> decorate(MeshineryInputSource<K, C> inputConnector) {
    return new TestConnectorDecorator<>(inputConnector, atomicInteger);
  }

  @RequiredArgsConstructor
  public static class TestConnectorDecorator<K, C extends MeshineryDataContext>
      implements MeshinerySourceConnector<K, C> {

    private final MeshineryInputSource<K, C> innerInputSource;
    private final AtomicInteger atomicInteger;

    @Override
    public String getName() {
      return innerInputSource.getName();
    }

    @Override
    public void writeOutput(K key, C output, TaskData taskData) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<C> getInputs(List<K> key) {
      atomicInteger.addAndGet(key.size());
      return innerInputSource.getInputs(key);
    }

    @Override
    public C commit(C context) {
      return context;
    }
  }
}
