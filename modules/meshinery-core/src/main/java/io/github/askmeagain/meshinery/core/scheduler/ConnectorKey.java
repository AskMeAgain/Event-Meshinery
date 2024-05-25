package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import lombok.Builder;
import lombok.Value;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder
public class ConnectorKey {
  MeshineryInputSource<Object, MeshineryDataContext> connector;
  Object key;
}
