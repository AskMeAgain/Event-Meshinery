package io.github.askmeagain.meshinery.core.utils.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TestInputSourceDecoratorFactory implements InputSourceDecoratorFactory {

  private final AtomicInteger atomicInteger;

  @Override
  public MeshineryConnector<?, DataContext> wrap(MeshineryConnector<?, ? extends DataContext> inputConnector) {
    return new TestConnectorDecorator(
        (MeshineryConnector<Object, DataContext>) inputConnector, atomicInteger);
  }

  @RequiredArgsConstructor
  public static class TestConnectorDecorator implements MeshineryConnector<Object, DataContext> {

    private final InputSource<Object, DataContext> innerInputSource;
    private final AtomicInteger atomicInteger;

    @Override
    public String getName() {
      return innerInputSource.getName();
    }

    @Override
    public void writeOutput(Object key, DataContext output) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<DataContext> getInputs(List<Object> key) {
      atomicInteger.addAndGet(key.size());
      return innerInputSource.getInputs(key);
    }
  }
}
