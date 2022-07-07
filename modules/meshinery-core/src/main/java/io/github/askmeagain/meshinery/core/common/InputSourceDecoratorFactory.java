package io.github.askmeagain.meshinery.core.common;


/**
 * A factory implementing this interface  will return a
 * decorated{@link io.github.askmeagain.meshinery.core.common.MeshineryConnector}.
 */
public interface InputSourceDecoratorFactory {
  MeshineryConnector<?, ? extends DataContext> decorate(MeshineryConnector<?, ? extends DataContext> inputConnector);

}
