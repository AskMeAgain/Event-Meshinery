package io.github.askmeagain.meshinery.monitoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.monitoring.grafanapush.MermaidJsonTemplatingEngine;
import io.github.askmeagain.meshinery.monitoring.grafanapush.MeshineryPushProperties;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "meshinery.draw.grafana-dashboard-push", name = "enabled")
public class MeshineryGrafanaPushConfiguration {

  private final ObjectMapper objectMapper;
  private final MeshineryPushProperties grafanaDashboardPush;
  private final List<MeshineryTask<?, ?>> tasks;

  @PostConstruct
  void pushingDashboard() throws IOException {

    requireNonNull(grafanaDashboardPush.getDashboardName());
    requireNonNull(grafanaDashboardPush.getGrafanaUrl());
    requireNonNull(grafanaDashboardPush.getMermaidDiagramUrl());
    requireNonNull(grafanaDashboardPush.getPassword());
    requireNonNull(grafanaDashboardPush.getMetricQuery());
    requireNonNull(grafanaDashboardPush.getUsername());

    try (var stream = this.getClass().getClassLoader().getResourceAsStream("mermaid-template.json")) {
      new MermaidJsonTemplatingEngine(requireNonNull(stream), grafanaDashboardPush, objectMapper, tasks).sendDashboardToGrafana();
    }
  }
}
