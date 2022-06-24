package io.github.askmeagain.meshinery.core.exceptions;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class DuplicateTaskNameException extends RuntimeException {
  public DuplicateTaskNameException(String message) {
    super(message);
  }
}
