package jp.co.freemind.csv.internal;

import java.util.List;
import java.util.StringJoiner;

/**
 * Created by kakusuke on 15/07/24.
 */
public class CsvLine {
  private final int lineNumber;
  private final List<String> fields;
  private final List<FieldFormatException> fieldErrors;

  public CsvLine(int lineNumber, List<String> fields, List<FieldFormatException> fieldErrors) {
    this.lineNumber = lineNumber;
    this.fields = fields;
    this.fieldErrors = fieldErrors;
  }

  public int getLineNumber() { return lineNumber; }
  public List<String> getFields() { return fields; }
  public List<FieldFormatException> getFieldErrors() { return fieldErrors; }

  public String toJson(String[] headerFields) {
    StringJoiner joiner = new StringJoiner(",");
    for (int i = 0, len = fields.size(); i < len; i++) {
      joiner.add(quote(headerFields[i]) + ":" + quote(fields.get(i)));
    }
    return "{" + joiner.toString() + "}";
  }

  public static class FieldFormatException extends RuntimeException {
    private final Exception cause;

    public FieldFormatException(Exception e) {
      this.cause = e;
    }
  }

  private String quote(String str) {
    return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
