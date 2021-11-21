package ask.me.again.meshinery.monitoring;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitoringTest {

  public static final CollectorRegistry registry = new CollectorRegistry();

  public static void registerNewGauge(String name, String helpText, Supplier<Double> value) {

    var gauge = Gauge.build()
        .name(name)
        .help(helpText)
        .register(registry);

    var child = new Gauge.Child() {
      @Override
      public double get() {
        return value.get();
      }
    };

    gauge.setChild(child);
  }

  @SneakyThrows
  public static String getMetrics() {
    ByteArrayOutputStream response = new ByteArrayOutputStream();
    OutputStreamWriter osw = new OutputStreamWriter(response, StandardCharsets.UTF_8);

    TextFormat.writeFormat(TextFormat.CONTENT_TYPE_004, osw, registry.metricFamilySamples());

    osw.close();

    return response.toString();
  }

}
