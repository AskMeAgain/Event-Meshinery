package io.github.askmeagain.meshinery.aop.common;

/**
 * Decide on the retry mechanism for a Meshinery AOP job
 */
public enum RetryMethod {

  /**
   * Does the retry in memory using a loop and trycatch statement
   */
  MEMORY,
  /**
   * Uses internal event queue for retry. Will create new events & jobs internally
   */
  EVENT,
  /**
   * Do not have any retry mechanism
   */
  NONE
}
