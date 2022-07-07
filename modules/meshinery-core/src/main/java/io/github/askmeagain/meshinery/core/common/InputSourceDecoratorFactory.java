package io.github.askmeagain.meshinery.core.common;


@SuppressWarnings("checkstyle:MissingJavadocType")
public interface InputSourceDecoratorFactory {
  MeshineryConnector<?, ? extends DataContext> wrap(MeshineryConnector<?, ? extends DataContext> inputConnector);

}
