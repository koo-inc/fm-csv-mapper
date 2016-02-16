package jp.co.freemind.csv;

import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.freemind.csv.internal.MixInCollector;
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
    CRLF("\r\n"),
    ;

    @Getter private final String value;
    LineBreak(String value) { this.value = value;}
  }

  private final Class<T> targetClass;
  private final Class<?> formatClass;
  private final Charset charset;
  private final boolean bomRequired;
  private final char fieldSeparator;
  private final LineBreak lineBreak;
  private final char quoteChar;
  private final char escapeChar;
  private final boolean headerRequired;
  private final String nullValue;
  private final boolean bareFieldIfPossible;
  private final String[] orderPaths;

  public String[] getHeaderFields() {
    return formatClass.getAnnotation(JsonPropertyOrder.class).value();
  }

  private static final MixInCollector MIX_IN_COLLECTOR = new MixInCollector();
  public void initMixIn(ObjectMapper objectMapper) {
    for (MixInCollector.Pair pair : MIX_IN_COLLECTOR.collect(getTargetClass(), getFormatClass())) {
      objectMapper.addMixIn(pair.getSource(), pair.getMixin());
    }
  }

  public Builder<T> builder() {
    return new Builder<>(this);
  }

  public static <T> Builder<T> builder(Class<T> targetClass) {
    return new Builder<>(targetClass);
  }

  public static class Builder<T> {
    private Class<T> targetClass;
    private Class<?> formatClass;

    @Setter @Accessors(fluent = true) private String charset = "UTF-8";
    @Setter @Accessors(fluent = true) private char columnSeparator = ',';
    @Setter @Accessors(fluent = true) private LineBreak lineBreak = LineBreak.CRLF;
    @Setter @Accessors(fluent = true) private char escapeChar = '\\';
    @Setter @Accessors(fluent = true) private String nullValue = "";
    @Setter @Accessors(fluent = true) private boolean bareFieldIfPossible = false;
    private boolean withBom = false;
    private boolean headerRequired = false;
    private Character quoteChar = '"';
    private String[] orderPaths;

    private Builder(Class<T> targetClass) {
      this.targetClass = targetClass;
      this.formatClass = targetClass;
    }

    public Builder (CsvFormatter<T> formatter) {
      this.targetClass = formatter.targetClass;
      this.formatClass = formatter.formatClass;
      this.charset = formatter.charset.name();
      this.columnSeparator = formatter.fieldSeparator;
      this.lineBreak = formatter.lineBreak;
      this.escapeChar = formatter.escapeChar;
      this.nullValue = formatter.nullValue;
      this.bareFieldIfPossible = formatter.bareFieldIfPossible;
      this.withBom = formatter.bomRequired;
      this.headerRequired = formatter.headerRequired;
      this.quoteChar = formatter.quoteChar;
      this.orderPaths = formatter.orderPaths;
    }

    public Builder<T> with(Class<?> formatClass) {
      this.formatClass = formatClass;
      return this;
    }

    public Builder<T> quoteChar(char quoteChar) {
      this.quoteChar = quoteChar;
      return this;
    }

    public Builder<T> withBom() {
      withBom = true;
      return this;
    }
    public Builder<T> withoutBom() {
      withBom = false;
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
    public Builder<T> orderBy(String... orderPaths) {
      this.orderPaths = orderPaths;
      return this;
    }

    public CsvFormatter<T> build() {
      return new CsvFormatter<>(
        targetClass,formatClass,
        Charset.forName(charset), withBom,
        columnSeparator,
        lineBreak, quoteChar, escapeChar, headerRequired, nullValue, bareFieldIfPossible, orderPaths
      );
    }
  }
}
