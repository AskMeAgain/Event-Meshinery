package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;


@SuppressWarnings("checkstyle:MissingJavadocType")
public interface ConnectorDecoratorFactory {
  MeshineryConnector<?, ? extends DataContext> wrap(MeshineryConnector<?, ? extends DataContext> processor);

}
