package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.freemind.csv.CsvFormatter;

import static java.util.stream.Collectors.joining;

/**
 * Created by kakusuke on 15/07/28.
 */
public class CsvBuilder<T> {
  private static final ObjectMapper plainMapper = new ObjectMapper();
  private static final TypeReference<Map<String, String>> typeReference = new TypeReference<Map<String, String>>() {};
  private final CsvFormatter<T> csvFormatter;
  private final ObjectMapper objectMapper;

  public CsvBuilder(Class<T> targetClass) {
    this(targetClass, targetClass);
  }
  public CsvBuilder(Class<T> targetClass, Class<?> formatClass) {
    this(CsvFormatter.builder(targetClass).with(formatClass).build());
  }
  public CsvBuilder(CsvFormatter<T> csvFormatter) {
    this(csvFormatter, plainMapper);
  }
  public CsvBuilder(CsvFormatter<T> csvFormatter, ObjectMapper objectMapper) {
    this.csvFormatter = csvFormatter;
    this.objectMapper = objectMapper.copy();
    this.objectMapper.addMixIn(csvFormatter.getTargetClass(), csvFormatter.getFormatClass());
  }

  public Consumer<T> writeTo(OutputStream os) {
    CsvSchema schema = new CsvSchema(csvFormatter.getFormatClass(), csvFormatter.getNullValue());
    String[] headers = schema.getHeaders();
    String headerLine = Arrays.stream(headers).map(this::quote).collect(joining(new String(new char[] {csvFormatter.getFieldSeparator()})));
    String separator = new String(new char[] { csvFormatter.getFieldSeparator() });
    byte[] lineBreak = csvFormatter.getLineBreak().getValue().getBytes(csvFormatter.getCharset());
    AtomicBoolean isFirst = new AtomicBoolean(true);
    return t -> {
      try {
        Map<String, String> data = plainMapper.readValue(objectMapper.writeValueAsString(t), typeReference);

        StringJoiner joiner = new StringJoiner(separator);
        for (String header : headers) {
          joiner.add(quote(data.get(header)));
        }

        if (!isFirst.getAndSet(false)) {
          os.write(lineBreak);
        }
        else if (csvFormatter.withHeader()) {
          os.write(headerLine.getBytes(csvFormatter.getCharset()));
          os.write(lineBreak);
        }
        os.write(joiner.toString().getBytes(csvFormatter.getCharset()));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };
  }

  private String quote(String str) {
    if (str == null) return csvFormatter.getNullValue();
    StringBuilder buffer = new StringBuilder();
    String quote = new String(new char[] {csvFormatter.getQuoteChar()});
    String escape = new String(new char[] {csvFormatter.getEscapeChar()});
    buffer.append(quote);
    str.chars().forEach(c -> {
      if (c == csvFormatter.getEscapeChar() || c == csvFormatter.getQuoteChar()) {
        buffer.append(escape);
      }
      buffer.append((char) c);
    });
    buffer.append(quote);
    return buffer.toString();
  }
}
