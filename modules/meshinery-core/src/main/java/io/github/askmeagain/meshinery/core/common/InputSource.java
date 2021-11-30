package io.github.askmeagain.meshinery.core.common;

import java.util.List;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface InputSource<K, I extends DataContext> {

  String getName();

  List<I> getInputs(K key);

}
