package io.github.askmeagain.meshinery.monitoring;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
public class MeshineryMonitoringService {

  public static final CollectorRegistry registry = new CollectorRegistry();
  public static final Summary requestTimeSummary = Summary.build()
      .name("request_time")
      .help("Time of all requests")
      .labelNames("task_name")
      .register(registry);

  public static final Gauge inProcessingGauge = Gauge.build()
      .name("processing_counter")
      .help("Number of all currently in work processors")
      .labelNames("task_name")
      .register(registry);

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static Gauge createGauge(String name, String helpText, String labelNames) {
    return Gauge.build()
        .name(name)
        .help(helpText)
        .labelNames(labelNames)
        .register(registry);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static void createGauge(String name, String helpText, Supplier<Double> value) {

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

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  public static String getMetrics() {
    ByteArrayOutputStream response = new ByteArrayOutputStream();
    OutputStreamWriter osw = new OutputStreamWriter(response, StandardCharsets.UTF_8);

    TextFormat.writeFormat(TextFormat.CONTENT_TYPE_004, osw, registry.metricFamilySamples());

    osw.close();

    return response.toString();
  }

}
