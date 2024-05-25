package io.github.askmeagain.meshinery.core.common;


/**
 * A factory implementing this interface  will return a
 * decorated{@link io.github.askmeagain.meshinery.core.common.InputSource}.
 */
public interface InputSourceDecoratorFactory {
  InputSource<?, ? extends DataContext> decorate(InputSource<?, ? extends DataContext> inputConnector);

}
