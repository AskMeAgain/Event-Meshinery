package io.github.askmeagain.meshinery.core.common;


/**
 * A factory implementing this interface  will return a
 * decorated{@link MeshineryInputSource}.
 */
public interface InputSourceDecorator<K, C extends MeshineryDataContext> {

  MeshineryInputSource<K, C> decorate(MeshineryInputSource<K, C> inputConnector);

}
