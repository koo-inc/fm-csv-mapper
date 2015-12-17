package jp.co.freemind.csv.internal;

import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
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
    return field().sepBy(fieldSeparator());
  }

  private Parser<String> field() {
    return quotedField().or(bareField());
  }

  private Parser<String> quotedField() {
    return Parsers.between(quote(), quotedString().source(), quote());
  }

  private Parser<String> bareField() {
    String sepStr = Pattern.quote(String.valueOf(fieldSeparator));
    String quoteStr = Pattern.quote(String.valueOf(quoteChar));
    return Patterns.regex("[^" + sepStr + quoteStr + "]*").toScanner("bare field").source();
  }

  private Parser<Void> quotedString() {
    String quoteStr = Pattern.quote(String.valueOf(quoteChar));
    String escapeStr = Pattern.quote(String.valueOf(escapeChar));
    return Patterns.regex("(" + escapeStr + ".|[^" + quoteStr + escapeStr + "])").many().toScanner("quoted string");
  }

  private Parser<Void> quote() {
    return Scanners.isChar(quoteChar);
  }

  private Parser<Void> fieldSeparator() {
    String sepStr = Pattern.quote(String.valueOf(fieldSeparator));
    return Patterns.regex("\\s*" + sepStr + "\\s*").toScanner("field separator");
  }

}
