package ask.me.again.meshinery.monitoring;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;

public class MonitoringTest {

  public static final CollectorRegistry registry = new CollectorRegistry();
  static final Counter requests = Counter.build()
      .name("requests_total").help("Total requests.").register(registry);

  public static void processRequest() {
    requests.inc();
    // Your code here.
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
