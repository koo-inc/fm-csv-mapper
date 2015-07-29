package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jp.co.freemind.csv.CsvFormatter;
import jp.co.freemind.csv.Location;
import jp.co.freemind.csv.exception.FieldFormatException;
import jp.co.freemind.csv.exception.LineParseException;
import jp.co.freemind.csv.exception.ReflectiveOperationRuntimeException;

import static java.util.stream.Collectors.toList;


/**
 * Created by kakusuke on 15/07/27.
 */
public class CsvParser<T> {

  private final CsvFormatter<T> formatter;
  private final ObjectMapper objectMapper;

  public CsvParser(CsvFormatter<T> csvFormatter) {
    this(csvFormatter, new ObjectMapper());
  }

  public CsvParser(CsvFormatter<T> csvFormatter, ObjectMapper objectMapper) {
    this.formatter = csvFormatter;
    this.objectMapper = objectMapper.copy();
  }

  public Stream<T> parse(InputStream is) {
    Set<Location> errorLocation = new LinkedHashSet<>();

    Stream<CsvLine> stream = parseToCsvLine(is);
    stream.onClose(()-> {
      if (errorLocation.size() > 0) {
        throw new FieldFormatException(errorLocation.stream().collect(toList()));
      }
    });

    ObjectMapper mapper = objectMapper.copy();
    mapper.addMixIn(formatter.getTargetClass(), formatter.getFormatClass());

    ObjectReader reader = mapper.readerFor(formatter.getTargetClass());

    CsvSchema schema = new CsvSchema(formatter.getFormatClass());

    return stream.map(line -> {
      line.getException().ifPresent(e ->
        errorLocation.add(new Location(line.getLineNumber(), OptionalInt.empty())));

      Set<String> ignoreField = new HashSet<>();
      while(true) {
        try {
          return reader.readValue(schema.toJson(line, ignoreField));
        }
        catch (InvalidFormatException e) {
          String fieldName = e.getPath().get(0).getFieldName();
          Location location = new Location(line.getLineNumber(), OptionalInt.of(schema.getColumnNumber(fieldName)));
          if (errorLocation.contains(location)) {
            throw new IllegalStateException("invalid row state: " + e.getLocation());
          }
          errorLocation.add(location);
          ignoreField.add(fieldName);
        }
        catch (IOException e) {
          errorLocation.add(new Location(line.getLineNumber(), OptionalInt.empty()));
          try {
            return formatter.getTargetClass().newInstance();
          }
          catch (ReflectiveOperationException e2) {
            throw new ReflectiveOperationRuntimeException(e2);
          }
        }
      }
    });
  }

  private Stream<CsvLine> parseToCsvLine(InputStream is) {
    CsvLineParser lineParser = new CsvLineParser(formatter.getQuoteChar(), formatter.getEscapeChar(), formatter.getFieldSeparator());

    int skipCount = formatter.withHeader() ? 1 : 0;
    AtomicInteger lineNum = new AtomicInteger(skipCount + 1);
    return parseToLine(is).skip(skipCount)
      .map(line -> {
        int i = lineNum.getAndIncrement();
        try {
          return new CsvLine(i, lineParser.parse(line));
        } catch (Exception e) {
          return new CsvLine(i, new LineParseException(e));
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
}
