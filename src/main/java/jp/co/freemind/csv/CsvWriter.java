package jp.co.freemind.csv;

import java.io.OutputStream;
import java.util.function.Consumer;

import jp.co.freemind.csv.internal.CsvBuilder;

/**
 * Created by kakusuke on 15/07/29.
 */
public class CsvWriter<T> {
  private final CsvBuilder<T> builder;

  CsvWriter(CsvBuilder<T> builder) {
    this.builder = builder;
  }

  public Consumer<T> writeTo(OutputStream os) {
    return builder.writeTo(os);
  }
}
