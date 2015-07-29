package jp.co.freemind.csv;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

import jp.co.freemind.csv.internal.CsvErrorSniffer;
import jp.co.freemind.csv.internal.CsvParser;

/**
 * Created by kakusuke on 15/07/29.
 */
public class CsvReader<T> {
  private final CsvParser<T> parser;
  private final CsvErrorSniffer sniffer = new CsvErrorSniffer();

  CsvReader(CsvParser<T> parser) {
    this.parser = parser;
  }

  public Set<Location> getErrorLocations() {
    return sniffer.getLocations();
  }

  public Stream<T> read(InputStream is) {
    return parser.parse(is, sniffer);
  }
}
