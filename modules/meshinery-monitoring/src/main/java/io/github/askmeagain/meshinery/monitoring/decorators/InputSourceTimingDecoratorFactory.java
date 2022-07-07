package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InputSourceTimingDecoratorFactory implements InputSourceDecoratorFactory {

  @Override
  public MeshineryConnector<?, DataContext> wrap(MeshineryConnector<?, ? extends DataContext> inputConnector) {
    return new ConnectorTimingDecorator((MeshineryConnector<Object, DataContext>) inputConnector);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorTimingDecorator implements MeshineryConnector<Object, DataContext> {

    private final MeshineryConnector<Object, DataContext> innerConnector;
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

    @Override
    public void writeOutput(Object key, DataContext output) {
      var connectorName = innerConnector.getName();
      var histogram = MeshineryMonitoringService.CONNECTOR_HISTOGRAM_OUT.labels(connectorName, key.toString());

      try (var timer = histogram.startTimer()) {
        try {
          innerConnector.writeOutput(key, output);
        } finally {
          timer.observeDuration();
        }
      }
    }
  }
}
