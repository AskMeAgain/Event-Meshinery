package io.github.askmeagain.meshinery.core.common;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface MeshineryConnector<K, C extends DataContext> extends OutputSource<K, C>, InputSource<K, C> {
}
