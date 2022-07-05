package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.ConnectorDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public class ConnectorTimingDecoratorFactory implements ConnectorDecoratorFactory {

  @Override
  public MeshineryConnector<?, DataContext> wrap(MeshineryConnector<?, ? extends DataContext> innerConnector) {
    return new ConnectorTimingDecorator((MeshineryConnector<Object, DataContext>) innerConnector);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorTimingDecorator implements MeshineryConnector<Object, DataContext> {

    private final MeshineryConnector<Object, DataContext> innerConnector;

    @Override
    public List<DataContext> getInputs(List<Object> keys) {
      var connectorName = innerConnector.getName();
      var keyNames = MeshineryUtils.joinEventKeys(keys);
      var summary = MeshineryMonitoringService.CONNECTOR_HISTOGRAM_IN.labels(connectorName, keyNames);

      var timer = summary.startTimer();

      try {
        return innerConnector.getInputs(keys);
      } finally {
        timer.observeDuration();
      }
    }

    @Override
    public String getName() {
      return innerConnector.getName();
    }

    @Override
    public void writeOutput(Object key, DataContext output) {

      var connectorName = innerConnector.getName();
      var histogram = MeshineryMonitoringService.CONNECTOR_HISTOGRAM_OUT.labels(connectorName, key.toString());

      var timer = histogram.startTimer();

      try {
        innerConnector.writeOutput(key, output);
      } finally {
        timer.observeDuration();
      }
    }
  }
}
