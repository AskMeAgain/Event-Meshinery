package io.github.askmeagain.meshinery.core.exceptions;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class DuplicateReadKeyException extends RuntimeException {
  public DuplicateReadKeyException(String message) {
    super(message);
  }
}
