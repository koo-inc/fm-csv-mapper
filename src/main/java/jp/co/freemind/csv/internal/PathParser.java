package jp.co.freemind.csv.internal;

import lombok.Value;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.Patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kakusuke on 15/11/05.
 */
class PathParser {
  private final Parser<List<PathSegment>> parser;

  public PathParser() {
    this.parser = path();
  }

  public List<PathSegment> parse(String line) {
    return parser.parse(line);
  }

  private Parser<PathSegment> startPathSegment() {
    return string().map(ObjectPathSegment::of);
  }

  private Parser<PathSegment> arrayPathSegment() {
    Parser<PathSegment> node = Patterns.regex("[0-9]+").toScanner("integer").source().map(ArrayPathSegment::of);
    return node.between(leftBracket(), rightBracket());
  }
  private Parser<PathSegment> objectPathSegment() {
    return period().next(string().map(ObjectPathSegment::of));
  }
  private Parser<PathSegment> childPathSegment() {
    return arrayPathSegment().or(objectPathSegment());
  }

  private Parser<List<PathSegment>> path() {
    return Parsers.sequence(startPathSegment(), childPathSegment().atLeast(0), this::reduce);
  }

  private Parser<Void> period() {
    return Scanners.isChar('.');
  }
  private Parser<Void> leftBracket() {
    return Scanners.isChar('[');
  }
  private Parser<Void> rightBracket() {
    return Scanners.isChar(']');
  }
  private Parser<String> string() {
    return Patterns.regex("[^.\\[\\]]+").toScanner("string literal").source();
  }

  private List<PathSegment> reduce(PathSegment a, List<PathSegment> b) {
    List<PathSegment> ret = new ArrayList<>();
    ret.add(a);
    ret.addAll(b);
    return ret;
  }


  interface PathSegment {
    String getName();
    boolean isArray();
  }
  @Value(staticConstructor = "of")
  static class ArrayPathSegment implements PathSegment {
    private final String name;
    private final boolean array = true;
  }
  @Value(staticConstructor = "of")
  static class ObjectPathSegment implements PathSegment {
    private final String name;
    private final boolean array = false;
  }
}
