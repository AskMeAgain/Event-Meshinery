package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class InputSourceTimingDecoratorFactory implements InputSourceDecoratorFactory {

  @Override
  public MeshineryInputSource<?, MeshineryDataContext> decorate(
      MeshineryInputSource<?, ? extends MeshineryDataContext> inputConnector
  ) {
    return new ConnectorTimingDecorator((MeshineryInputSource<Object, MeshineryDataContext>) inputConnector);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ConnectorTimingDecorator implements MeshineryInputSource<Object, MeshineryDataContext> {

    private final MeshineryInputSource<Object, MeshineryDataContext> innerConnector;
    @Getter(lazy = true)
    private final String name = innerConnector.getName();

    @Override
    public List<MeshineryDataContext> getInputs(List<Object> keys) {
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
