package io.github.askmeagain.meshinery.connectors.docker;

import io.github.askmeagain.meshinery.core.common.DataContext;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class DockerDataContext implements DataContext {

  String Id = UUID.randomUUID().toString();
  String log;

}
