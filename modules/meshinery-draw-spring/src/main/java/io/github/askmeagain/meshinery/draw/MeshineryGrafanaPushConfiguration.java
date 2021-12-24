package io.github.askmeagain.meshinery.draw;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "meshinery.draw.grafana-dashboard-push", name = "enabled")
public class MeshineryGrafanaPushConfiguration {

  private final ObjectMapper objectMapper;
  private final MeshineryDrawProperties meshineryDrawProperties;
  private final List<MeshineryTask<?, ?>> tasks;

  @PostConstruct
  void pushingDashboard() throws IOException {
    try (var stream = this.getClass().getClassLoader().getResourceAsStream("mermaid-template.json")) {
      new MermaidJsonTemplatingEngine(
          Objects.requireNonNull(stream),
          meshineryDrawProperties.getGrafanaDashboardPush(),
          objectMapper,
          tasks
      ).send();
    }
  }
}
