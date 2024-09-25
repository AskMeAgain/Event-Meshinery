package io.github.askmeagain.meshinery.core.common;

/**
 * Context interface. Each context needs to have a unique id.
 */
public interface MeshineryDataContext {

  /**
   * unique id of the context.
   *
   * @return id
   */
  String getId();

  /**
   * Returns the metadata value for a given key.
   *
   * @param key the key
   * @return returns the value if found, returns null if it doesnt
   */
  String getMetadata(String key);

  /**
   * Add key/value to metadata for each context.
   * @param key metadata key
   * @param value metadata value
   * @return returns a new version with the updated metadata
   */
  <C extends MeshineryDataContext> C setMetadata(String key, String value);
}
