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
}
