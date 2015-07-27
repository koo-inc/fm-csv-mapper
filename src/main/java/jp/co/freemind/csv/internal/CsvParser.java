package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jp.co.freemind.csv.CsvFormatter;


/**
 * Created by kakusuke on 15/07/27.
 */
public class CsvParser<T> {

  private final CsvFormatter<T> formatter;

  public CsvParser(CsvFormatter<T> csvFormatter) {
    this.formatter = csvFormatter;
  }

  public Stream<T> parse(InputStream is) {
    Stream<CsvLine> stream = parseToCsvLine(is);
    stream.onClose(()-> {
      throw new RuntimeException("hoge");
    });
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(formatter.getTargetClass(), formatter.getFormatClass());
    ObjectReader reader = mapper.readerFor(formatter.getTargetClass());
    return stream.map(line -> {
      try {
        return reader.readValue(line.toJson(formatter.getHeaderFields()));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private Stream<String> parseToLine(InputStream is) {
    CsvScanner scanner = new CsvScanner(is, formatter.getCharset(), formatter.getQuoteChar(), formatter.getEscapeChar());

    int characteristic = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    Spliterator<String> spliterator = Spliterators.spliteratorUnknownSize(new CsvLineIterator(scanner), characteristic);

    RunnableUtil.ThrowingRunnable<IOException> close = scanner::close;
    return StreamSupport.stream(spliterator, false).onClose(close.ignoreThrown(UncheckedIOException::new));
  }
  private Stream<CsvLine> parseToCsvLine(InputStream is) {
    CsvLineParser lineParser = new CsvLineParser(formatter.getQuoteChar(), formatter.getEscapeChar(), formatter.getFieldSeparator());

    AtomicInteger lineNum = new AtomicInteger(1);
    return parseToLine(is)
      .map(line -> {
        int i = lineNum.getAndIncrement();
        try {
          return new CsvLine(i, lineParser.parse(line), Collections.emptyList());
        } catch (Exception e) {
          return new CsvLine(i, Collections.emptyList(), Collections.singletonList(new CsvLine.FieldFormatException(e)));
        }
      });
  }
}
