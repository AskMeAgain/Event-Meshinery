package io.github.askmeagain.meshinery.monitoring.decorators;

import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class InputSourceTimingDecoratorFactory<K, C extends MeshineryDataContext>
    implements InputSourceDecorator<K, C> {

  @Override
  public MeshineryInputSource<K, C> decorate(MeshineryInputSource<K, C> inputConnector) {
    return new TimingDecorator<>(inputConnector);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class TimingDecorator<K, C extends MeshineryDataContext> implements MeshineryInputSource<K, C> {

    private final MeshineryInputSource<K, C> innerConnector;
    @Getter(lazy = true)
    private final String name = innerConnector.getName();

    @Override
    public C commit(C context) {
      return context;
    }

    @Override
    public List<C> getInputs(List<K> keys) {
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
