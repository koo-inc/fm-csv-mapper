package jp.co.freemind.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import jp.co.freemind.csv.internal.CsvBuilder;
import jp.co.freemind.csv.internal.CsvParser;

/**
 * Created by kakusuke on 15/07/29.
 */
public class CsvMapper<T> {
  private final CsvParser<T> parser;
  private final CsvBuilder<T> builder;

  public CsvMapper(Class<T> targetClass) {
    this(targetClass, new ObjectMapper());
  }
  public CsvMapper(Class<T> targetClass, Class<?> formatClass) {
    this(targetClass, formatClass, new ObjectMapper());
  }
  public CsvMapper(Class<T> targetClass, ObjectMapper mapper) {
    this(CsvFormatter.builder(targetClass).build(), mapper);
  }
  public CsvMapper(Class<T> targetClass, Class<?> formatClass, ObjectMapper mapper) {
    this(CsvFormatter.builder(targetClass).with(formatClass).build(), mapper);
  }

  public CsvMapper(CsvFormatter<T> formatter, ObjectMapper mapper) {
    this.parser = new CsvParser<>(formatter, mapper);
    this.builder = new CsvBuilder<>(formatter, mapper);
  }

  public CsvReader<T> createReader() {
    return new CsvReader<>(parser);
  }

  public CsvWriter<T> createWriter() {
    return new CsvWriter<>(builder);
  }
}
