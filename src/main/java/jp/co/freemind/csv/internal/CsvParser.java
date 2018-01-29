package jp.co.freemind.csv.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jp.co.freemind.csv.CsvFormatter;
import jp.co.freemind.csv.CsvValidator;
import jp.co.freemind.csv.Location;
import jp.co.freemind.csv.exception.LineParseException;
import jp.co.freemind.csv.exception.ReflectiveOperationRuntimeException;
import jp.co.freemind.csv.exception.ValidationException;


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

  public Stream<T> parse(InputStream is, CsvErrorSniffer context) {
    return parse(is, context, t -> {});
  }

  public Stream<T> parse(InputStream is, CsvErrorSniffer context, CsvValidator<T> validator) {
    ObjectMapper mapper = objectMapper.copy();
    formatter.initMixIn(mapper);

    ObjectReader reader = mapper.readerFor(formatter.getTargetClass());

    CsvSchema schema = new CsvSchema(formatter);

    return parseToCsvLine(is).map(line -> {
      line.getException().ifPresent(e ->
        context.mark(new Location(line.getLineNumber(), schema.getLineIndex(line.getLineNumber()))));

      Set<String> ignoreField = new HashSet<>();
      T t;
      while (true) {
        try {
          t =  reader.readValue(schema.toJson(line, ignoreField));
          break;
        } catch (JsonMappingException e) {
          String path = buildPath(e.getPath());
          Location location = new Location(line.getLineNumber(), schema.getLineIndex(line.getLineNumber()), schema.getColumnNumber(path), path, schema.getColumnIndex(path));
          if (context.contains(location)) {
            throw new IllegalStateException("invalid row state: " + e.getLocation());
          }
          context.mark(location);
          ignoreField.add(path);
        } catch (IOException e) {
          context.mark(new Location(line.getLineNumber(), schema.getLineIndex(line.getLineNumber())));
          try {
            return formatter.getTargetClass().newInstance();
          } catch (ReflectiveOperationException e2) {
            throw new ReflectiveOperationRuntimeException(e2);
          }
        }
      }

      try {
        validator.validate(t);
      } catch (ValidationException e) {
        e.getViolation().forEach((path, message) -> {
          Location location = new Location(line.getLineNumber(), schema.getLineIndex(line.getLineNumber()), schema.getColumnNumber(path), path, schema.getColumnIndex(path));
          context.mark(location, message);
        });
      }
      return t;
    });
  }

  private Stream<CsvLine> parseToCsvLine(InputStream is) {
    CsvLineParser lineParser = new CsvLineParser(formatter.getQuoteChar(), formatter.getEscapeChar(), formatter.getFieldSeparator());

    int skipCount = formatter.isHeaderRequired() ? formatter.getSkipLineCount() : 0;
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

  private String buildPath(List<JsonMappingException.Reference> refs) {
    StringBuilder sb = new StringBuilder();
    JsonMappingException.Reference prev = null;
    for(JsonMappingException.Reference ref : refs) {
      if (prev != null && ref.getFieldName() != null) {
        sb.append('.');
      }

      if (ref.getFieldName() != null) {
        sb.append(ref.getFieldName());
      }
      else {
        sb.append("[").append(ref.getIndex()).append("]");
      }
      prev = ref;
    }
    return sb.toString();
  }

}
