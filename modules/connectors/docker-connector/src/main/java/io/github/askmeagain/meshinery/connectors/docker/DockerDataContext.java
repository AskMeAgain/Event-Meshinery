package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.common.DataContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@RequiredArgsConstructor
public class DockerDataContext implements DataContext {

  String Id = UUID.randomUUID().toString();
  @With
  String log;

}
