package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import lombok.Builder;
import lombok.Value;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder
public class ConnectorKey {
  InputSource<Object, DataContext> connector;
  Object key;
}
