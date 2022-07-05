package io.github.askmeagain.meshinery.core.utils.decorators;

import io.github.askmeagain.meshinery.core.common.ConnectorDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


public class TestConnectorDecoratorFactory implements ConnectorDecoratorFactory {

  @Override
  public MeshineryConnector<?, DataContext> wrap(MeshineryConnector<?, ? extends DataContext> processor) {
    return new TestConnectorDecorator((MeshineryConnector<Object, DataContext>) processor);
  }

  @RequiredArgsConstructor
  public static class TestConnectorDecorator implements MeshineryConnector<Object, DataContext> {

    private final MeshineryConnector<Object, DataContext> innerConnector;

    @Override
    public String getName() {
      return innerConnector.getName();
    }

    @Override
    public void writeOutput(Object key, DataContext output) {
      innerConnector.writeOutput(key, output);
    }

    @Override
    public List<DataContext> getInputs(List<Object> key) {
      return innerConnector.getInputs(key);
    }
  }
}
