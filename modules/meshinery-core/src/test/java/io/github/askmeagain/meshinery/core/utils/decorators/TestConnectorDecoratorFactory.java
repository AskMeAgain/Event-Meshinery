package io.github.askmeagain.meshinery.core.utils.decorators;

import io.github.askmeagain.meshinery.core.common.ConnectorDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


public class TestConnectorDecoratorFactory implements ConnectorDecoratorFactory<Object,DataContext> {

  @Override
  public MeshineryConnector<Object, DataContext> wrap(MeshineryConnector<?, DataContext> processor) {
    return new TestConnectorDecorator((MeshineryConnector<Object, DataContext>) processor);
  }
}
