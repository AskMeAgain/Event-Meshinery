package io.github.askmeagain.meshinery.core.common;


/**
 * Connector interface. A combination of input and output source for easier MeshineryTask setup.
 *
 * @param <K> event key type
 * @param <C> context type
 */
public interface MeshinerySourceConnector<K, C extends MeshineryDataContext> extends MeshineryOutputSource<K, C>,
    MeshineryInputSource<K, C> {
}
