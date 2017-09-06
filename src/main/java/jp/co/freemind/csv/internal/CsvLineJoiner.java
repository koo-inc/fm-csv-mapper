package jp.co.freemind.csv.internal;

import jp.co.freemind.csv.CsvFormatter;

public class CsvLineJoiner {
  private final char separator;
  private final char quote;
  private final char escape;
  private final String nullValue;
  private final boolean allowBareField;
  private final StringBuilder builder;
  private boolean firstColumn = true;

  public CsvLineJoiner(CsvFormatter<?> formatter) {
    this(formatter.getFieldSeparator(), formatter.getQuoteChar(), formatter.getEscapeChar(), formatter.getNullValue(), formatter.isBareFieldIfPossible());
  }

  public CsvLineJoiner(char separator, char quote, char escape, String nullValue, boolean allowBareField) {
    this.separator = separator;
    this.quote = quote;
    this.escape = escape;
    this.nullValue = nullValue;
    this.allowBareField = allowBareField;
    this.builder = new StringBuilder();
  }

  public CsvLineJoiner append(String value) {
    if (this.firstColumn) {
      this.firstColumn = false;
    }
    else {
      this.builder.append(separator);
    }
    builder.append(quote(value));
    return this;
  }

  @Override
  public String toString() {
    return builder.toString();
  }

  private String quote(String str) {
    if (str == null) return nullValue;
    StringBuilder buffer = new StringBuilder();
    boolean needsQuote = needsQuote(str);
    if (needsQuote) {
      buffer.append(quote);
    }
    str.chars().forEach(c -> {
      if (c == escape || c == quote) {
        buffer.append(escape);
      }
      buffer.append((char) c);
    });
    if (needsQuote) {
      buffer.append(quote);
    }
    return buffer.toString();
  }

  private boolean needsQuote(String str) {
    if (!allowBareField) return true;

    for (int i = 0, len = str.length(); i < len; i++) {
      char c = str.charAt(i);
      if (c == quote) return true;
      if (c == escape) return true;
      if (c == separator) return true;
    }
    return false;
  }
}
