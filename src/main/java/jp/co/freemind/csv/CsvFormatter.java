package jp.co.freemind.csv;

import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Created by kakusuke on 15/07/20.
 */
@Value
public class CsvFormatter<T> {
  public enum LineBreak {
    CR("\r"),
    LF("\n"),
    CRLF("\r\n")
    ;

    @Getter private final String value;
    LineBreak(String value) { this.value = value;}
  }

  private final Class<T> targetClass;
  private final Class<?> formatClass;
  private final boolean quoteRequired ;
  private final Charset charset;
  private final char fieldSeparator;
  private final LineBreak lineBreak;
  private final char quoteChar;
  private final char escapeChar;
  private final boolean headerRequired;
  private final String nullValue;

  public boolean withHeader() {
    return headerRequired;
  }

  public String[] getHeaderFields() {
    return formatClass.getAnnotation(JsonPropertyOrder.class).value();
  }

  public static <T> Builder<T> builder(Class<T> targetClass) {
    return new Builder<>(targetClass);
  }

  public static class Builder<T> {
    private final Class<T> targetClass;
    private Class<?> formatClass;

    @Setter @Accessors(fluent = true) private String charset = "MS932";
    @Setter @Accessors(fluent = true) private char columnSeparator = ',';
    @Setter @Accessors(fluent = true) private LineBreak lineBreak = LineBreak.CRLF;
    @Setter @Accessors(fluent = true) private char escapeChar = '\\';
    @Setter @Accessors(fluent = true) private String nullValue;
    private boolean headerRequired = true;
    private Character quoteChar = '"';

    private Builder(Class<T> targetClass) {
      this.targetClass = targetClass;
      this.formatClass = targetClass;
    }

    public Builder<T> with(Class<?> formatClass) {
      this.formatClass = formatClass;
      return this;
    }

    public Builder<T> quoteChar(char quoteChar) {
      this.quoteChar = quoteChar;
      return this;
    }
    public Builder<T> withoutQuoteChar() {
      this.quoteChar = 0;
      return this;
    }

    public Builder<T> withHeaders() {
      this.headerRequired = true;
      return this;
    }
    public Builder<T> withoutHeader() {
      this.headerRequired = false;
      return this;
    }

    public CsvFormatter<T> build() {
      return new CsvFormatter<>(
        targetClass,formatClass,
        quoteChar > 0,
        Charset.forName(charset),
        columnSeparator,
        lineBreak, quoteChar, escapeChar, headerRequired, nullValue
      );
    }
  }
}
