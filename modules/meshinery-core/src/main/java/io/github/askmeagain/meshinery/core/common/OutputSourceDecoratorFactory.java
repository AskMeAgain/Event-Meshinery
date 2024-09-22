package io.github.askmeagain.meshinery.core.common;


/**
 * A factory implementing this interface  will return a
 * decorated{@link MeshineryInputSource}.
 */
public interface OutputSourceDecoratorFactory {
  MeshineryOutputSource<?, ? extends MeshineryDataContext> decorate(
      MeshineryOutputSource<?, ? extends MeshineryDataContext> outputSource
  );

}
