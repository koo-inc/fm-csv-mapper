package jp.co.freemind.csv.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
  private final Map<String, String> violation = new LinkedHashMap<>();

  public ValidationException() {
  }

  public ValidationException(Map<String, String> violation) {
    this.violation.putAll(violation);
  }

  public ValidationException put(String name, String message) {
    this.violation.put(name, message);
    return this;
  }

  public Map<String, String> getViolation() {
    return violation;
  }
}
