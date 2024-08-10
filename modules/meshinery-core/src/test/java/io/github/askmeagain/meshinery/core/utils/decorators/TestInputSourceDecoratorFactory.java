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
public class TestInputSourceDecoratorFactory implements InputSourceDecoratorFactory {

  private final AtomicInteger atomicInteger;

  @Override
  public MeshinerySourceConnector<?, MeshineryDataContext> decorate(
      MeshineryInputSource<?, ? extends MeshineryDataContext> inputConnector
  ) {
    return new TestConnectorDecorator(
        (MeshinerySourceConnector<Object, MeshineryDataContext>) inputConnector, atomicInteger);
  }

  @RequiredArgsConstructor
  public static class TestConnectorDecorator implements MeshinerySourceConnector<Object, MeshineryDataContext> {

    private final MeshineryInputSource<Object, MeshineryDataContext> innerInputSource;
    private final AtomicInteger atomicInteger;

    @Override
    public String getName() {
      return innerInputSource.getName();
    }

    @Override
    public void writeOutput(Object key, MeshineryDataContext output, TaskData taskData) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<MeshineryDataContext> getInputs(List<Object> key) {
      atomicInteger.addAndGet(key.size());
      return innerInputSource.getInputs(key);
    }

    @Override
    public MeshineryDataContext commit(MeshineryDataContext context) {
      return context;
    }
  }
}
