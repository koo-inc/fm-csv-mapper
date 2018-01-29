package jp.co.freemind.csv;

import java.util.Optional;
import java.util.OptionalInt;

import lombok.EqualsAndHashCode;

/**
 * Created by kakusuke on 15/07/27.
 */
@EqualsAndHashCode(exclude = {"columnName"})
public class Location {
  private final int lineNumber;
  private final Integer columnNumber;
  private final String columnName;
  private final int lineIndex;
  private final int columnIndex;

  @Deprecated
  @java.beans.ConstructorProperties({"lineNumber", "columnNumber", "columnName", "withHeader"})
  public Location(int lineNumber, Integer columnNumber, String columnName, boolean withHeader) {
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.columnName = columnName;
    this.lineIndex =  lineNumber - (withHeader ? 2 : -1);
    this.columnIndex = columnNumber != null ? columnNumber - 1 : -1;
  }

  @java.beans.ConstructorProperties({"lineNumber", "lineIndex"})
  public Location(int lineNumber, int lineIndex) {
    this.lineNumber = lineNumber;
    this.lineIndex =  lineIndex;
    this.columnNumber = null;
    this.columnName = null;
    this.columnIndex = -1;
  }

  @java.beans.ConstructorProperties({"lineNumber", "lineIndex", "columnNumber", "columnName", "columnIndex"})
  public Location(int lineNumber, int lineIndex, Integer columnNumber, String columnName, int columnIndex) {
    this.lineNumber = lineNumber;
    this.lineIndex =  lineIndex;
    this.columnNumber = columnNumber;
    this.columnName = columnName;
    this.columnIndex = columnIndex;
  }

  @Deprecated
  @java.beans.ConstructorProperties({"lineNumber", "columnNumber"})
  public Location(int lineNumber, OptionalInt columnNumber) {
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber.isPresent() ? columnNumber.getAsInt() : null;
    this.columnName = null;
    this.lineIndex = lineNumber - 1;
    this.columnIndex = columnNumber.orElse(-1);
  }

  @Override
  public String toString() {
    return "(line: " + lineNumber +
      (columnNumber != null ? ", column: " + columnNumber : "") +
      (columnName != null ? " [" + columnName + "]" : "") +
      ')';
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  public OptionalInt getColumnNumber() {
    return this.columnNumber != null ? OptionalInt.of(this.columnNumber) : OptionalInt.empty();
  }

  public int getLineIndex() {
    return this.lineIndex;
  }

  public int getColumnIndex() {
    return this.columnIndex;
  }

  public Optional<String> getColumnName() {
    return Optional.ofNullable(this.columnName);
  }
}
