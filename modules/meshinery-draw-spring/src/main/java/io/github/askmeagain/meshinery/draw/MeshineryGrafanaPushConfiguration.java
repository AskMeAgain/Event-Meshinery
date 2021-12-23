package io.github.askmeagain.meshinery.draw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MeshineryGrafanaPushConfiguration {

  private final MeshineryDrawProperties drawProperties;

  @PostConstruct
  void pushingDashboard() throws IOException {
    var stream = this.getClass().getClassLoader().getResourceAsStream("mermaid-template.json");
    var in = Objects.requireNonNull(stream);

    try (var isr = new InputStreamReader(in); var reader = new BufferedReader(isr)) {
      var strings = reader.lines().toList();
      new MermaidJsonTemplatingEngine(strings, drawProperties).send();
    }
  }
}
