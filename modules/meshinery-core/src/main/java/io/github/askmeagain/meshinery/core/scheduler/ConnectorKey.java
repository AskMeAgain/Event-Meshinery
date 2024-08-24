package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConnectorKey {

  MeshineryInputSource connector;

  @EqualsAndHashCode.Include
  List<?> key;
}
