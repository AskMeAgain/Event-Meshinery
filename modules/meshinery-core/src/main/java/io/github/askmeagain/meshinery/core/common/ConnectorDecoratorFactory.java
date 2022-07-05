package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;


@SuppressWarnings("checkstyle:MissingJavadocType")
public interface ConnectorDecoratorFactory<K, O extends DataContext> {
  MeshineryConnector<K, O> wrap(MeshineryConnector<?, O> processor);

}
