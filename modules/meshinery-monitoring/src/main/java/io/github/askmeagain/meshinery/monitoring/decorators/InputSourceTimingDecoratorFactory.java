package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class InputSourceTimingDecoratorFactory implements InputSourceDecoratorFactory {

  @Override
  public InputSource<?, DataContext> decorate(InputSource<?, ? extends DataContext> inputConnector) {
    return new ConnectorTimingDecorator((InputSource<Object, DataContext>) inputConnector);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorTimingDecorator implements InputSource<Object, DataContext> {

    private final InputSource<Object, DataContext> innerConnector;
    @Getter(lazy = true)
    private final String name = innerConnector.getName();

    @Override
    public List<DataContext> getInputs(List<Object> keys) {
      var connectorName = innerConnector.getName();
      var keyNames = MeshineryUtils.joinEventKeys(keys);
      var summary = MeshineryMonitoringService.CONNECTOR_HISTOGRAM_IN.labels(connectorName, keyNames);

      try (var timer = summary.startTimer()) {
        try {
          return innerConnector.getInputs(keys);
        } finally {
          timer.observeDuration();
        }
      }
    }
  }
}
