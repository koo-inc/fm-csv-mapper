package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.freemind.csv.CsvFormatter;

/**
 * Created by kakusuke on 15/07/28.
 */
public class CsvBuilder<T> {
  private static final ObjectMapper flattenMapper = new ObjectMapper().registerModule(new FlattenModule());
  private static final TypeReference<Map<String, String>> typeReference = new TypeReference<Map<String, String>>() {};
  private static final Charset UTF8 = Charset.forName("UTF-8");
  private final CsvFormatter<T> csvFormatter;
  private final ObjectMapper objectMapper;
  private final String[] headerFields;

  public CsvBuilder(Class<T> targetClass) {
    this(targetClass, targetClass);
  }
  public CsvBuilder(Class<T> targetClass, Class<?> formatClass) {
    this(CsvFormatter.builder(targetClass).with(formatClass).build());
  }
  public CsvBuilder(CsvFormatter<T> csvFormatter) {
    this(csvFormatter, flattenMapper);
  }
  public CsvBuilder(CsvFormatter<T> csvFormatter, ObjectMapper objectMapper) {
    this(csvFormatter, objectMapper, null);
  }
  private CsvBuilder(CsvFormatter<T> csvFormatter, ObjectMapper objectMapper, String[] headerFields) {
    this.csvFormatter = csvFormatter;
    this.objectMapper = objectMapper.copy();
    this.headerFields = headerFields;
    csvFormatter.initMixIn(this.objectMapper);
  }

  public CsvBuilder<T> withHeader(String[] headerFields) {
    return new CsvBuilder<>(csvFormatter.builder().withHeaders().build(), objectMapper, headerFields);
  }
  public CsvBuilder<T> orderBy(String... orderPath) {
    return new CsvBuilder<>(csvFormatter.builder().orderBy(orderPath).build(), objectMapper, headerFields);
  }

  public Consumer<T> writeTo(OutputStream os) {
    CsvSchema schema = new CsvSchema(csvFormatter);
    String[] propertyNames = schema.getPropertyNames();

    CsvOutputStreamWriter cos = new CsvOutputStreamWriter(os, csvFormatter);

    if (csvFormatter.isHeaderRequired()) {
      try {
        cos.write(joinHeaders(propertyNames));
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    return t -> {
      try {
        Map<String, String> data = flattenMapper.readValue(objectMapper.writeValueAsBytes(t), typeReference);

        CsvLineJoiner joiner = new CsvLineJoiner(csvFormatter);
        String[] orderPaths = csvFormatter.getOrderPaths() != null ? csvFormatter.getOrderPaths() : propertyNames;
        for (String path : orderPaths) {
          joiner.append(data.get(path));
        }

        cos.write(joiner.toString());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };
  }

  private String joinHeaders(String[] propertyNames) {
    if (propertyNames == null) return "";
    CsvLineJoiner joiner = new CsvLineJoiner(csvFormatter);
    for (String headerField : (headerFields != null ? headerFields : propertyNames)) {
      joiner.append(headerField);
    }
    return joiner.toString();
  }

  private void setupOutputStream(OutputStream os, String headerLine) {
    try {
      if (csvFormatter.getCharset().equals(UTF8) && csvFormatter.isBomRequired()) {
        os.write(0xEF);
        os.write(0xBB);
        os.write(0xBF);
      }
      if (csvFormatter.isHeaderRequired()) {
        os.write(headerLine.getBytes(csvFormatter.getCharset()));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
