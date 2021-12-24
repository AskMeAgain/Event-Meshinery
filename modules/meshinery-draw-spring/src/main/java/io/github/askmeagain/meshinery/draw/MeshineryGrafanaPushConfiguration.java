package io.github.askmeagain.meshinery.draw;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "meshinery.draw.grafana-dashboard-push", name = "enabled")
public class MeshineryGrafanaPushConfiguration {

  private final ObjectMapper objectMapper;
  private final MeshineryDrawProperties meshineryDrawProperties;
  private final List<MeshineryTask<?, ?>> tasks;

  @PostConstruct
  void pushingDashboard() throws IOException {

    var grafanaDashboardPush = meshineryDrawProperties.getGrafanaDashboardPush();

    requireNonNull(grafanaDashboardPush.getDashboardName());
    requireNonNull(grafanaDashboardPush.getGrafanaUrl());
    requireNonNull(grafanaDashboardPush.getMermaidDiagramUrl());
    requireNonNull(grafanaDashboardPush.getPassword());
    requireNonNull(grafanaDashboardPush.getMetricQuery());
    requireNonNull(grafanaDashboardPush.getUsername());

    try (var stream = this.getClass().getClassLoader().getResourceAsStream("mermaid-template.json")) {
      new MermaidJsonTemplatingEngine(requireNonNull(stream), grafanaDashboardPush, objectMapper, tasks).send();
    }
  }
}
