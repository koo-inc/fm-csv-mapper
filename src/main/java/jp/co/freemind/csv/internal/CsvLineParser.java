package jp.co.freemind.csv.internal;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Pattern;
import org.codehaus.jparsec.pattern.Patterns;

/**
 * Created by kakusuke on 15/07/24.
 */
public class CsvLineParser {
  private final Parser<List<String>> parser;
  private final char quoteChar;
  private final char escapeChar;
  private final char fieldSeparator;

  public CsvLineParser(char quoteChar, char escapeChar, char fieldSeparator) {
    this.quoteChar = quoteChar;
    this.escapeChar = escapeChar;
    this.fieldSeparator = fieldSeparator;

    this.parser = fields();
  }

  public List<String> parse(String line) {
    return parser.parse(line);
  }

  private Parser<List<String>> fields() {
    return field().sepBy(fieldSeparator().toScanner("field separator"));
  }

  private Parser<String> field() {
    String esc = java.util.regex.Pattern.quote(String.valueOf(escapeChar));
    String quot = java.util.regex.Pattern.quote(String.valueOf(quoteChar));
    return quotedField().map(s -> s.replaceAll(esc + "([" + esc + quot + "])", "$1")).or(bareField());
  }

  private Parser<String> quotedField() {
    Parser<Void> quote = quote().toScanner("quote");
    return Parsers.between(quote, quotedString().source(), quote);
  }

  private Parser<String> bareField() {
    return Patterns.many(
      CharPredicates.and(
        CharPredicates.notChar(quoteChar),
        CharPredicates.notChar(escapeChar),
        CharPredicates.notChar(fieldSeparator)
      )
    ).toScanner("bare field").source();
  }

  private Parser<Void> quotedString() {
    return Patterns.or(
      Patterns.sequence(escape(), quote()),
      Patterns.sequence(escape(), escape()),
      Patterns.many(
        CharPredicates.and(
          CharPredicates.notChar(quoteChar),
          CharPredicates.notChar(escapeChar)
        )
      )
    ).many().toScanner("quoted string");
  }

  private Pattern quote() {
    return Patterns.isChar(quoteChar);
  }

  private Pattern escape() {
    return Patterns.isChar(escapeChar);
  }

  private Pattern fieldSeparator() {
    return Patterns.regex("\\s*" + java.util.regex.Pattern.quote(String.valueOf(fieldSeparator)) + "\\s*");
  }

}
