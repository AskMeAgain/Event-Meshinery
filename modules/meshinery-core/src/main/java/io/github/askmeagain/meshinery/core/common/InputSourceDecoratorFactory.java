package io.github.askmeagain.meshinery.core.common;


/**
 * A factory implementing this interface  will return a
 * decorated{@link MeshineryInputSource}.
 */
public interface InputSourceDecoratorFactory {
  MeshineryInputSource<?, ? extends MeshineryDataContext> decorate(
      MeshineryInputSource<?, ? extends MeshineryDataContext> inputConnector
  );

}
