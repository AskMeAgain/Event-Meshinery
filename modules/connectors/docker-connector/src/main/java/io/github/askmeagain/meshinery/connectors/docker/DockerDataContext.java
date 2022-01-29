package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.common.DataContext;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class DockerDataContext implements DataContext {
  @Getter
  String Id;

  List<String> logs;
}
