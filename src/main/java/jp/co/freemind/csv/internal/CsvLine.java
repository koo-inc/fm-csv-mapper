package jp.co.freemind.csv.internal;

import java.util.List;
import java.util.Optional;

import jp.co.freemind.csv.exception.LineParseException;
import lombok.NonNull;
import lombok.Value;

/**
 * Created by kakusuke on 15/07/24.
 */
@Value
public class CsvLine {
  private final int lineNumber;
  private final String[] fields;
  private final Optional<LineParseException> exception;

  public CsvLine(int lineNumber, @NonNull List<String> fields) {
    this.lineNumber = lineNumber;
    this.fields = fields.toArray(new String[fields.size()]);
    this.exception = Optional.empty();
  }
  public CsvLine(int lineNumber, @NonNull LineParseException exception) {
    this.lineNumber = lineNumber;
    this.fields = new String[0];
    this.exception = Optional.of(exception);
  }
}
