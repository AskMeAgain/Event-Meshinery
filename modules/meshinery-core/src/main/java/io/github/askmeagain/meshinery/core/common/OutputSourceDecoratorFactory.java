package io.github.askmeagain.meshinery.core.common;


/**
 * A factory implementing this interface  will return a
 * decorated{@link InputSource}.
 */
public interface OutputSourceDecoratorFactory {
  OutputSource<?, ? extends DataContext> decorate(OutputSource<?, ? extends DataContext> inputConnector);

}
