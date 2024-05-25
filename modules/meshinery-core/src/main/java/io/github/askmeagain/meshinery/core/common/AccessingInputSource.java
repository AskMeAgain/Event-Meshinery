package io.github.askmeagain.meshinery.core.common;

import java.util.Optional;

/**
 * Meshinery sources with this interface enable access to a specific context by event key + id.
 *
 * @param <K> The key
 * @param <C> The id
 */
@SuppressWarnings("checkstyle:MissingJavadocType")
public interface AccessingInputSource<K, C extends MeshineryDataContext> extends MeshineryInputSource<K, C> {

  /**
   * Get access to a specific context via key + id.
   * The context is getting removed from the internal queue and is not able to be processed by a normal input source
   *
   * @param key the event key to be used
   * @param id  the id of the context
   * @return Optional of the context
   */
  Optional<C> getContext(K key, String id);

}
